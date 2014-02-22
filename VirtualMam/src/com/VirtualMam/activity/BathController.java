package com.VirtualMam.activity;

import org.json.JSONException;
import org.json.JSONObject;

import com.VirtualMam.activity.ExtendService;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class BathController extends DeviceController {
	/** 湯張りステータス 湯張りしていない状態 */
	public static final int BATH_NO_YUHARI = 0;
	/** 湯張りステータス 湯張り中 */
	public static final int BATH_NOW_YUHARI = 1;
	/** 湯張りステータス 湯張り完了 */
	public static final int BATH_READY = 2;
	/** 人がお風呂に入っている状態 */
	public static final int BATH_IN_BATH = 3;
	/** 湯張りステータス お風呂済ませた状態 */
	public static final int BATH_FINISHED = 4;

	/** 機器ニックネーム */
	public static final String NICKNAME = "ElectricWaterHeater";

	private int status;
	private Context context;

	public BathController(Context context) {
		this.status = BATH_NO_YUHARI;
		this.context = context;
	}

	/**
	 * 湯張り開始
	 */
	public void startYuhari() {
		// スマートウォッチに通知
		Intent i = new Intent(context, ExtendService.class);
		i.setAction(ExtendService.INTENT_ACTION_NOTIFY);
		i.putExtra(ExtendService.EXTEND_KEY, "お風呂ためといたよ！");
		context.startService(i);
		
		set("[" + NICKNAME + ",[0xE3,[0x41]]]", new DeviceListener() {
			@Override
			public void onResponse(JSONObject result) {
				status = BATH_NOW_YUHARI;
				// 湯張り量監視開始
				new BathWatcher().execute();
			}
		});
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	class BathWatcher extends AsyncTask<String, Integer, String> {

		protected static final String TAG = "BathWatcher";

		@Override
		protected String doInBackground(String... params) {

			// 風呂の湯張り状態をチェック
			while(true) {
				try {
					BathController.this.get("[" + NICKNAME + ",0xE3]", new DeviceListener() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								JSONObject result = response.getJSONObject("result");
								JSONObject property = result.getJSONArray("property").getJSONObject(0);
								int status = property.getJSONArray("value").getInt(0);

								switch (status) {
								case 0x41: // 湯張り中
									break;

								case 0x42: // 湯張り完了
									// スマートウォッチに通知
									Intent i = new Intent(context, ExtendService.class);
									i.setAction(ExtendService.INTENT_ACTION_NOTIFY);
									i.putExtra(ExtendService.EXTEND_KEY, "お風呂が沸きました");
									context.startService(i);
									status = BATH_READY;
									return;

								default:
									Log.e(TAG, "bad status");
								}

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
