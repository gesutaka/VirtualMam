package com.VirtualMam.activity;

import java.io.*;
import java.text.*;
import java.util.*;

import android.media.*;
import android.os.*;
import android.provider.*;
import android.provider.MediaStore.Images;
import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera.Face;
import android.hardware.Camera.Size;
import android.hardware.Camera;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	WebView controlUnit;
	
	Camera camera;
	Camera.Parameters params;
	
	OverlayImageView cameraOverlayView;
	ImageView focusImageView;
	Animation focusAnimation;
	
	Thread imageProcThread;
	Thread takePictureThread;
	Thread addMotionThread;
	
	boolean takePictureFlg = false; 
	boolean savePictureFlg = false;
	boolean focusingOnFlg = false;
	
	int displayDegree = 90;
	
	CameraSurfaceView(Context context) {
		super(context);
		initialize();
	}
	
	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	@SuppressWarnings("deprecation")
	private void initialize() {
		Log.e("Camera Surface View","Camera Surface View: initialize");
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				
		try {
			camera = Camera.open();
			params = camera.getParameters();
			setOptimalPictureSize(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setFocusImageView(ImageView view, Animation anime) {
		focusImageView = view;
		focusAnimation = anime;		
	}
		
	public void setCameraOvarlayView(OverlayImageView overlayView) {
		cameraOverlayView = overlayView;
	}
	
	public void waitThreads() {		
		try {
			if(imageProcThread != null) imageProcThread.join();
			Log.e("Wait Threads","Wait Threads: Preview Callback Thread");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			if(takePictureThread != null) takePictureThread.join();
			Log.e("Wait Threads","Wait Threads: Picture Callback Thread");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			if(addMotionThread != null) addMotionThread.join();
			Log.e("Wait Threads","Wait Threads: Add Motion Thread");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void releaseCallbacks() {
		if(camera == null) return;
		
		camera.setPreviewCallback(null);
		camera.setFaceDetectionListener(null);
		camera.setAutoFocusMoveCallback(null);
	}
	
	public void releaseCamera() {
		if(camera == null) return;
		
		camera.release();
		camera = null;
	}
	
	public Camera.Parameters getParams() {
		return params;
	}
	
	public void setParams(Camera.Parameters params) {
		if(takePictureThread != null && takePictureThread.isAlive()) return;
		
		this.params = params;
		camera.setParameters(params);
		
		setOptimalPictureSize(false);
	}

	public void takePicture() {
		if(takePictureThread != null && takePictureThread.isAlive()) return;
		
		Log.e("Take Picture","Take Picture: Flg On");
		focusOn();
		takePictureFlg = true;
	}
	
	public void focusOn() {
		if(focusingOnFlg) return;
		Log.e("focus On","focus On: Flg On");
		focusingOnFlg = true;
		
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(params);
		camera.cancelAutoFocus();
		
		focusImageView.setVisibility(View.VISIBLE);
		focusImageView.startAnimation(focusAnimation);
		
		camera.autoFocus(new CameraAutoFocusCallback());
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e("Camera Surface View","Camera Surface View: Created");
		if (camera == null) return;
		
		try {
			camera.setPreviewDisplay(holder);
			
			camera.setPreviewCallback(new CameraPreviewCallback());
			
			camera.setFaceDetectionListener(new CameraFaceDetectionListener());
			camera.setAutoFocusMoveCallback(new CameraAutoFocusMoveCallback());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e("Camera Surface View","Camera Surface View: Destroyed");
		if (camera == null) return;
		
		camera.stopPreview();
	}
	
	@SuppressWarnings("deprecation")
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { 
		Log.e("Camera Surface View","Camera Surface View: Changed");
		Display display = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		displayDegree = 0;
		switch (display.getRotation()) {
			case Surface.ROTATION_0: displayDegree = 90; break;
			case Surface.ROTATION_90: displayDegree = 0; break;
			case Surface.ROTATION_180: displayDegree = 270; break;
			case Surface.ROTATION_270: displayDegree = 180; break;
		}
		camera.setDisplayOrientation(displayDegree);
		cameraOverlayView.setDisplayDegree(displayDegree);
		
		int displayWidth = (displayDegree == 90) ? display.getHeight() : display.getWidth();
		int displayHeight = (displayDegree == 90) ? display.getWidth() : display.getHeight();
		
		Size previewSize = getOptimalPreviewSize(displayWidth, displayHeight);
		params.setPreviewSize(previewSize.width, previewSize.height);
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		camera.setParameters(params);
		
		startCameraPreview();
	}
	
	private boolean startCameraPreview() {
		try {
			camera.startPreview();
			camera.startFaceDetection();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private Size getOptimalPreviewSize(int width, int height) {
		List<Size> supportedPreviewSizes = params.getSupportedPreviewSizes();
		
		Size optimalSize = null;
		double maxSize = Double.MIN_VALUE;
		
		for (Size size : supportedPreviewSizes) {
			int minOfSize = Math.min(size.height, size.width);
			double defOfAspect = Math.abs(height / width - size.height / size.width);
			if(maxSize < minOfSize && defOfAspect < 0.1) {
				optimalSize = size;
				maxSize = minOfSize;
			}
		}

		return optimalSize;
	}
	
	private void setOptimalPictureSize(boolean init) {
		Size pictureSize = params.getPictureSize();
		List<Size> supportedPictureSizes = params.getSupportedPictureSizes();
		
		if(!init && Math.max(pictureSize.width, pictureSize.height) <= 1280) {
			return;
		}
		
		double maxSize = Double.MIN_VALUE;
		
		for (Size size : supportedPictureSizes) {
			int maxOfSize = Math.max(size.height, size.width);
			if(maxSize < maxOfSize && maxOfSize <= 1280) {
				pictureSize = size;
				maxSize = maxOfSize;
			}
		}
		
		params.setPictureSize(pictureSize.width, pictureSize.height);
		camera.setParameters(params);
	}
	
	private class CameraPreviewCallback implements Camera.PreviewCallback, Runnable {
		
		byte[] previewData;
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {			
			if (data == null) return;
			
			if(savePictureFlg) {
				Log.e("Save Picture","Save Picture: Flg Off");
				savePictureFlg = false;
				Toast.makeText(CameraSurfaceView.this.getContext(), "Saved", Toast.LENGTH_SHORT).show();
			}
			
			if(imageProcThread != null && imageProcThread.isAlive())
				return;
			
			previewData = data;
			imageProcThread = new Thread(this);
			imageProcThread.start();
		}

		@Override
		public void run() {
			Log.e("Preview Callback","Preview Callback: Thread Proccesing");
			
			int width = params.getPreviewSize().width;
			int height = params.getPreviewSize().height;
			
			YuvImage yuvImage = new YuvImage(previewData, params.getPreviewFormat(), width, height, null);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outStream);
			
			try { outStream.close(); } catch (Exception e) { e.printStackTrace(); }
			
			Matrix settingMatrix = new Matrix();
			settingMatrix.setRotate(displayDegree);
			if (displayDegree == 90) {
				settingMatrix.postScale((float)getWidth() / height, (float)getHeight() / width);
			} else {
				settingMatrix.postScale((float)getWidth() / width, (float)getHeight() / height);
			}
			
			Bitmap previewBitmap = Bitmap.createBitmap(
					BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.size()),
					0, 0, width, height, settingMatrix, true);
			
			cameraOverlayView.setPreviewImage(previewBitmap);
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private class CameraPictureCallback implements Camera.PictureCallback, Runnable {
		
		byte[] pictureData;
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.e("Picture Callback","Picture Callback: Called");
			
			startCameraPreview();
			
			if (data == null) {
				return;
			}
			
			pictureData = data;
			takePictureThread = new Thread(this);
			takePictureThread.start();
		}
		
		@Override
		public void run() {
			Log.e("Picture Callbacke","Picture Callback: Thread Proccesing");
			
			Matrix rotateMatrix = new Matrix();
			rotateMatrix.setRotate(displayDegree);
			
			Bitmap pictureBitmap = Bitmap.createBitmap(
					BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length),
					0,
					0,
					params.getPictureSize().width,
					params.getPictureSize().height,
					rotateMatrix,
					true);
			
			File saveDir = new File(Environment.getExternalStorageDirectory().getPath() + "/AirControll");
			
			if (!saveDir.exists()) saveDir.mkdir();
			
			String dateTimeString = new SimpleDateFormat("yyyyMMdd_kkmmss").format(new Date());
			String imagePath = saveDir.getPath() + "/" + dateTimeString + ".jpg";
			
			deleteAndroidDB(imagePath);
			
			FileOutputStream fileOutStream;
			try {
				fileOutStream = new FileOutputStream(imagePath);
				pictureBitmap.compress(CompressFormat.JPEG, 100, fileOutStream);
				pictureBitmap.recycle();
				fileOutStream.close();
				
				registAndroidDB(imagePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Log.e("Save Picture","Save Picture: Flg On");
			savePictureFlg = true;
		}
		
		private void deleteAndroidDB(String path) {
			CameraSurfaceView.this.getContext().getContentResolver().delete(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			MediaStore.Images.Media.DATA + "=?",
			new String[] {path});
		}
		
		private void registAndroidDB(String path) {
			ContentValues values = new ContentValues();
			values.put(Images.Media.MIME_TYPE, "image/jpeg");
			values.put("_data", path);
			CameraSurfaceView.this.getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		}
	}
	
	private class CameraShutterCallback implements Camera.ShutterCallback {
		@Override
		public void onShutter() {
		}
	}
	
	private class CameraFaceDetectionListener implements Camera.FaceDetectionListener {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			cameraOverlayView.setFaces(faces);
		}
	}
	
	private class CameraAutoFocusCallback implements Camera.AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.e("Auto Focus Callback", "Auto Focus Callback: Status - " + ((success == true) ? "Starting" : "Stopped"));
			
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			camera.setParameters(params);
			camera.cancelAutoFocus();
		}
	}
	
	private class CameraAutoFocusMoveCallback implements Camera.AutoFocusMoveCallback {
		@Override
		public void onAutoFocusMoving(boolean start, Camera camera) {
			Log.e("Auto Focus Move Callback", "Auto Focus Move Callback: Status - " + ((start == true) ? "Starting" : "Stopped"));
			
			if(start) {
				Log.e("focus On","focus On: Flg On");
				focusingOnFlg = true;
				
				focusImageView.setVisibility(View.VISIBLE);
				focusImageView.startAnimation(focusAnimation);
			} else  {
				Log.e("focus On","focus On: Flg Off");
				focusingOnFlg = false;
				
				if(takePictureFlg) {
					Log.e("Take Picture","Take Picture: Flg Off");
					takePictureFlg = false;
					camera.takePicture(new CameraShutterCallback(), null, new CameraPictureCallback());
				}
				
				focusImageView.clearAnimation();
				focusImageView.setVisibility(View.INVISIBLE);
			}
		}
	}
}
