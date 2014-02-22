package com.VirtualMam.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExtendReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.d(ExtendService.LOG_TAG, "onReceive: " + intent.getAction());
		intent.setClass(context, ExtendService.class);
		context.startService(intent);
	}
}
