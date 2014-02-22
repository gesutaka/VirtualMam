package com.VirtualMam.activity;

import java.util.*;

import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;

import android.os.*;
import android.app.*;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera.Size;
import android.hardware.Camera;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	
	CameraSurfaceView cameraPreview;
	OverlayImageView cameraOverlayView;
	
	BroadReceiver mReceiver;

	TextView settingText;
	TextView settingZoom;
	ImageView zoomBase;
	ImageView zoomToggle;
	ImageView focusImage;
	Animation focusAnimation;
	
	// かーちゃん AI
	MotherAI motherAI;
	boolean IsAtHome = false;
	boolean IsOnTV = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("Camera Activity", "Camera Activity: Create");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camera);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		((ImageView)findViewById(R.id.camera_cameraShutter)).setOnClickListener(new clickImageView1());
		(settingText = (TextView)findViewById(R.id.camera_cameraSetting)).setVisibility(View.INVISIBLE);
		(settingZoom = (TextView)findViewById(R.id.camera_zoomSetting)).setVisibility(View.INVISIBLE);
		(focusImage = (ImageView)findViewById(R.id.camera_focusView)).setVisibility(View.INVISIBLE);
		(zoomBase = (ImageView)findViewById(R.id.camera_zoomBaseView)).setVisibility(View.INVISIBLE);
		(zoomToggle = (ImageView)findViewById(R.id.camera_zoomToggleView)).setVisibility(View.INVISIBLE);
		
		cameraOverlayView = (OverlayImageView)findViewById(R.id.camera_cameraOverlayView);
		focusAnimation = AnimationUtils.loadAnimation(this, R.layout.focus_animaiton);
		
		cameraPreview = (CameraSurfaceView)findViewById(R.id.camera_cameraPreView);
		cameraPreview.setFocusImageView(focusImage, focusAnimation);
		cameraPreview.setCameraOvarlayView(cameraOverlayView);
		cameraPreview.setOnTouchListener(new TouchSurface());
		
		mReceiver = new BroadReceiver();
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
		this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
		
		// かあちゃんAIのインスタンス
		motherAI = new MotherAI(this);
	}
	
	@Override
	protected void onRestart() {
		Log.e("Camera Activity", "Camera Activity: Restart");
		super.onRestart();
	}
	
	@Override
	protected void onStart() {
		Log.e("Camera Activity", "Camera Activity: Start");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		Log.e("Camera Activity", "Camera Activity: Resume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.e("Camera Activity", "Camera Activity: Pause");
		super.onPause();
		
		cameraPreview.waitThreads();
		cameraPreview.releaseCallbacks();
	}
	
	@Override
	protected void onStop() {
		Log.e("Camera Activity", "Camera Activity: Stop");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.e("Camera Activity", "Camera Activity: Destroy");
		super.onDestroy();
		
		NotificationUtil.deleteAllEvents(this);
		cameraPreview.releaseCamera();
	}
	
	private void HumanStatus(boolean flg) {
		IsAtHome = flg;
		
		motherAI.setAtHome(IsAtHome);
		
		Toast.makeText(
				getApplicationContext(),
				"Smart Watch " + ((IsAtHome) ? "" : "Dis-") + "Connected",
				Toast.LENGTH_LONG).show();
		
		Log.e("Broadcast Receiver",
				"Broadcast Receiver: Smart Watch " + ((IsAtHome) ? "" : "Dis-") + "Connected");
	}
	
	public void FaceStatus(boolean flg) {
		IsOnTV = flg;
		
		motherAI.setOnTV(IsOnTV);
		
		Toast.makeText(
				getApplicationContext(),
				"Face " + ((IsOnTV) ? "" : "Un-") + "Detected",
				Toast.LENGTH_LONG).show();
		
//		if(IsOnTV)
//			doExtendService("Face Detected");
//		else
//			doExtendService("Face Un-Detected");
		
		Log.e("Broadcast Receiver",
				"Broadcast Receiver: Face " + ((IsOnTV) ? "" : "Un-") + "Detected");
	}
	
	private void doExtendService(String mes) {
		Intent i = new Intent(this, ExtendService.class);
		i.setAction(ExtendService.INTENT_ACTION_NOTIFY);
		i.putExtra(ExtendService.EXTEND_KEY, mes);
		startService(i);
	}
	
	private class BroadReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if(device.getAddress().equals("B4:52:7D:F6:25:C5"))
					HumanStatus(true);
				
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if(device.getAddress().equals("B4:52:7D:F6:25:C5"))
					HumanStatus(false);
			}
		}
	}
	
	private class clickImageView1 implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			cameraPreview.takePicture();
		}
	}
	
	private class TouchSurface implements View.OnTouchListener {
		
		Camera.Parameters params;
		
		final int actionSensor = 15;
				
		int touchNowX;
		int touchNowY;
		int touchDownX;
		int touchDownY;
		int moveEnoughX;
		int moveEnoughY;
		
		List<Integer> supportedZoomList;
		int zoomIndex;
		int zoomNow = 0;
		int zoomMax;
		boolean zoomFlg = false;		

		List<String> supportedWhiteList;
		int whiteIndex;
		int whiteNow = 0;
		int whiteMax;
		boolean whiteFlg = false;
		
		List<Size> supportedPictureSizes;
		int sizeIndex;
		int sizeNow = 0;
		int sizeMax;
		boolean sizeFlg = false;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			touchNowX = (int)event.getRawX();
			touchNowY = (int)event.getRawY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				params = cameraPreview.getParams();
				touchDownX = touchNowX;
				touchDownY = touchNowY;
				
				break;
			
			case MotionEvent.ACTION_MOVE:
				
				if (!zoomFlg && !whiteFlg && !sizeFlg) {
					
					if (Math.abs(touchNowX - touchDownX) > actionSensor) {
						supportedZoomList = params.getZoomRatios();
						
						moveEnoughX = touchNowX;
						moveEnoughY = touchNowY;
						zoomIndex = zoomNow;
						zoomMax = params.getMaxZoom();
						zoomFlg = true;
						
						double scaleNow = 0.75 / zoomMax * zoomNow + 0.25;
						
						zoomBase.setTranslationX(moveEnoughX - zoomBase.getWidth() * (float)scaleNow);
						zoomBase.setTranslationY(moveEnoughY - zoomBase.getHeight() / 2);
						
						zoomToggle.setPivotY(zoomToggle.getHeight() / 2);
						zoomToggle.setScaleX((float)scaleNow);
						zoomToggle.setScaleY((float)scaleNow);
						zoomToggle.setTranslationX(zoomBase.getTranslationX());
						zoomToggle.setTranslationY(zoomBase.getTranslationY());
						
						settingZoom.setText(String.valueOf((double)(supportedZoomList.get(zoomNow) / 10) / 10) + "x");
						settingZoom.setTranslationX(zoomBase.getTranslationX() - settingZoom.getWidth() / 2);
						settingZoom.setTranslationY(moveEnoughY - settingZoom.getHeight() / 2);
						
						settingZoom.setVisibility(View.VISIBLE);
						zoomBase.setVisibility(View.VISIBLE);
						zoomToggle.setVisibility(View.VISIBLE);
					}
					else if(Math.abs(touchNowY - touchDownY) > actionSensor) {
						supportedWhiteList = params.getSupportedWhiteBalance();
						supportedPictureSizes = params.getSupportedPictureSizes();
						
						moveEnoughX = touchNowX;
						moveEnoughY = touchNowY;
						
						if(touchNowX < getWindowManager().getDefaultDisplay().getWidth() / 2) {							
							whiteIndex = whiteNow;
							whiteMax = supportedWhiteList.size() - 1;
							whiteFlg = true;
						} else {							
							sizeIndex = sizeNow;
							sizeMax = supportedPictureSizes.size() - 1;
							sizeFlg = true;
						}
						
						settingText.setText(
								supportedWhiteList.get(whiteNow) + "  |  " +
								String.valueOf(supportedPictureSizes.get(sizeNow).width) + " * " +
								String.valueOf(supportedPictureSizes.get(sizeNow).height));
						
						settingText.setVisibility(View.VISIBLE);
					}
				}
				else if(zoomFlg) {
					int setZoom = zoomIndex + (int)((touchNowX - moveEnoughX) / (zoomToggle.getWidth() * 0.75 / zoomMax));
					setZoom = (setZoom > zoomMax) ? zoomMax : (setZoom < 0) ? 0 : setZoom;
					if(zoomNow != setZoom)
					{
						zoomNow = setZoom;
						
						params.setZoom(zoomNow);
						cameraPreview.setParams(params);
						
						Log.e("Change Camera Setting", "Change Camera Setting: Zoom - " + zoomNow);
					}
					
					double scaleNow = 0.75 / zoomMax * setZoom + 0.25;
					
					zoomToggle.setScaleX((float)scaleNow);
					zoomToggle.setScaleY((float)scaleNow);
					
					settingZoom.setText(String.valueOf((double)(supportedZoomList.get(zoomNow) / 10) / 10) + "x");
				}
				else {
					if(whiteFlg) {
						int setWhite = whiteIndex - (touchNowY - moveEnoughY) / 30;
						setWhite = (setWhite > whiteMax) ? whiteMax : (setWhite < 0) ? 0 : setWhite;
						if(whiteNow != setWhite)
						{
							whiteNow = setWhite;
							
							params.setWhiteBalance(supportedWhiteList.get(whiteNow));
							cameraPreview.setParams(params);
							
							Log.e("Change Camera Setting", "Change Camera Setting: White Balance - " + whiteNow);
						}
					}
					else if(sizeFlg) {
						int setSize = sizeIndex + (touchNowY - moveEnoughY) / 30;
						setSize = (setSize > sizeMax) ? sizeMax : (setSize < 0) ? 0 : setSize;
						if(sizeNow != setSize)
						{
							sizeNow = setSize;
							
							params.setPictureSize(
									supportedPictureSizes.get(sizeNow).width,
									supportedPictureSizes.get(sizeNow).height);
							cameraPreview.setParams(params);
							
							Log.e("Change Camera Setting", "Change Camera Setting: Picture Size - " + sizeNow);
						}
					}
					
					settingText.setText(
							supportedWhiteList.get(whiteNow) + "  |  " +
							String.valueOf(supportedPictureSizes.get(sizeNow).width) + " * " +
							String.valueOf(supportedPictureSizes.get(sizeNow).height));
				}
				
				break;
				
			case MotionEvent.ACTION_UP:
				if(Math.abs(touchNowX - touchDownX) + Math.abs(touchNowY - touchDownY) < actionSensor)
					cameraPreview.focusOn();
				
				zoomFlg = false;
				whiteFlg = false;
				sizeFlg = false;
				
				settingZoom.setVisibility(View.INVISIBLE);
				settingText.setVisibility(View.INVISIBLE);
				zoomBase.setVisibility(View.INVISIBLE);
				zoomToggle.setVisibility(View.INVISIBLE);
				break;
			}
			
			return true;
		}
	}  
}