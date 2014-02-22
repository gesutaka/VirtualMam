package com.VirtualMam.activity;

import android.content.*;
import android.graphics.*;
import android.hardware.Camera.*;
import android.util.*;
import android.widget.*;

public class OverlayImageView extends ImageView {
	
	int displayDegree;
	
	Face[] detectFaces;
	Bitmap previewBitmap;
	Paint facePaint;
	
	int faceUndetectCont = 0;
	int faceTimeOut = 90;
	boolean isFacedetect = false;
	
	Camera camera;

	public OverlayImageView(Context context) {
		super(context);
		initialize();
	}
	
	public OverlayImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public OverlayImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
	
	private void initialize() {
		facePaint = new Paint();
		facePaint.setColor(Color.MAGENTA);
		facePaint.setAlpha(128);
		facePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}
	
	public void setFaces(Face[] faces) {
		detectFaces = faces;
		invalidate();
	}
	
	public void setDisplayDegree(int degree) {
		displayDegree = degree;
	}
	
	public void setPreviewImage(Bitmap prevImage) {
		previewBitmap = prevImage;
		
		/** TODO プレビュー画像を編集して表示させたりできます */
	}
		
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (detectFaces == null) {
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			return;
		}
		
		boolean isFace = false;
		for (Face face : detectFaces) {
			if (face == null) {
				continue;
			}
			
			isFace = true;
			
			RectF faceRect = new RectF(face.rect);
			
			Matrix rotateRect = new Matrix();
			rotateRect.setRotate(displayDegree, 0, 0);
			rotateRect.mapRect(faceRect);
			
			// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
			Matrix matrix = new Matrix();
			matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
			matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
			int saveCanvas = canvas.save();
			canvas.concat(matrix);
			canvas.drawRect(faceRect, facePaint);
			canvas.restoreToCount(saveCanvas);
		}
		
		// 初めて検出
		if(!isFacedetect && isFace) {
			isFacedetect = true;
			((MainActivity)OverlayImageView.this.getContext()).FaceStatus(isFacedetect);
		}
		// 検出されなかった
		else if(isFacedetect && !isFace) {
			faceUndetectCont++;
			
			//30frame以上経過
			if(faceUndetectCont > faceTimeOut) {
				faceUndetectCont = 0;
				isFacedetect = false;
				((MainActivity)OverlayImageView.this.getContext()).FaceStatus(isFacedetect);
			}
		}
		// 30frame以内に再検出
		else if(isFacedetect && isFace && faceUndetectCont < faceTimeOut) {
			faceUndetectCont = 0;
		}
	}
}