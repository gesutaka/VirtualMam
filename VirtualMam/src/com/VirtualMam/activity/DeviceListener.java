package com.VirtualMam.activity;

import org.json.JSONObject;

public interface DeviceListener {
	/**
	 * 家電制御結果受信時のコールバック
	 * @param result kodecotから帰ってきたレスポンスがJSONオブジェクトとして渡される
	 */
	public void onResponse(JSONObject result);
}
