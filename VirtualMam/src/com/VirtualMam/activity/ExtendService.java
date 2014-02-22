package com.VirtualMam.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.notification.Notification;
import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.notification.NotificationUtil;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

public class ExtendService extends ExtensionService {
	
	public static final String EXTEND_SPECIFIC_ID = "EXTEND_SPECIFIC_ID";
	public static final String EXTEND_KEY = "com.VirtualMam.key";
	public static final String LOG_TAG = "NotifyExtend";
	
	public static final String INTENT_ACTION_NOTIFY = "com.VirtualMam.action.notify";
	
	private static final String MAM_NAME = "かあちゃん";

	public ExtendService() {
		super(EXTEND_KEY);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int retVal = super.onStartCommand(intent, flags, startId);
		if (intent != null) {
			if(INTENT_ACTION_NOTIFY.equals(intent.getAction())) {
				Log.d(LOG_TAG, "onStart action: INTENT_ACTION_NOTIFY");
				addData(intent.getStringExtra(EXTEND_KEY));
				stopSelfCheck();
			}
		}

		return retVal;
	}

	private void addData(String message) {
		long time = System.currentTimeMillis();
		long sourceId = NotificationUtil
				.getSourceId(this, EXTEND_SPECIFIC_ID);
		
		String profileImage = ExtensionUtils.getUriString(this,
				R.drawable.widget_mam_picture);

		ContentValues eventValues = new ContentValues();
		eventValues.put(Notification.EventColumns.EVENT_READ_STATUS, false);
		eventValues.put(Notification.EventColumns.DISPLAY_NAME, MAM_NAME);
		eventValues.put(Notification.EventColumns.MESSAGE, message);
		eventValues.put(Notification.EventColumns.PERSONAL, 1);
		eventValues.put(Notification.EventColumns.PROFILE_IMAGE_URI, profileImage);
		eventValues.put(Notification.EventColumns.PUBLISHED_TIME, time);
		eventValues.put(Notification.EventColumns.SOURCE_ID, sourceId);
		
		getContentResolver().insert(Notification.Event.URI, eventValues);
	}
	
	@Override
	protected RegistrationInformation getRegistrationInformation() {
		return new RegistInformation(this);
	}

	@Override
	protected boolean keepRunningWhenConnected() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}
	
}
