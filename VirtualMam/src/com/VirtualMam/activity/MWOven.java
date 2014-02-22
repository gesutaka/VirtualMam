package com.VirtualMam.activity;

import org.json.JSONObject;

import com.VirtualMam.activity.ExtendService;
import android.content.Context;
import android.content.Intent;

public class MWOven extends DeviceController {

	/** 食事まだ */
	public static final int MEAL_NO = 0;
	/** 暖め完了 */
	public static final int MEAL_READY = 1;
	/** 食事中 */
	public static final int MEAL_NOW = 2;
	/** 食事完了 */
	public static final int MEAL_FINISHED = 3;

	/** 機器ニックネーム */
	public static final String NICKNAME = "CombinationMicrowaveOven";

	private int status;

	/**
	 * 食事開始
	 * @param context
	 */
	public void startMeal(final Context context) {
		// 加熱モード設定
		set("[" + NICKNAME + ",[0xE0,[0x41]]]", new DeviceListener() {
			@Override
			public void onResponse(JSONObject result) {
				// 加熱時間設定
				set("[" + NICKNAME + ",[0xE5,[0x00,0x01,0x00]]]", new DeviceListener() {
					@Override
					public void onResponse(JSONObject result) {
						// 出力設定
						set("[" + NICKNAME + ",[0xE7,[0x01,0xF4]]]", new DeviceListener() {
							@Override
							public void onResponse(JSONObject result) {
								// 加熱開始
								set("[" + NICKNAME + ",[0xB2,[0x41]]]", new DeviceListener() {
									@Override
									public void onResponse(JSONObject result) {
										// スマートウォッチに通知
										Intent i = new Intent(context, ExtendService.class);
										i.setAction(ExtendService.EXTEND_KEY);
										i.putExtra(ExtendService.EXTEND_KEY, "ご飯たべーや！");
										context.startService(i);
										status = MEAL_READY;
									}
								});
							}
						});
					}
				});
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

}
