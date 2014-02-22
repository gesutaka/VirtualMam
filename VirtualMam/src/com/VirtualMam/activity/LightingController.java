package com.VirtualMam.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

public class LightingController extends DeviceController {

	/** ライト消えてる状態 */
	public static final int LIGHTING_OFF = 0;

	/** ライトついてる状態 */
	public static final int LIGHTING_ON = 1;

	private int status;
	private ArrayList<String> nickNameList;

	public LightingController() {
		status = LIGHTING_OFF;
		nickNameList = new ArrayList<String>();
		nickNameList.add("GeneralLighting1");
		nickNameList.add("GeneralLighting2");
		nickNameList.add("GeneralLighting3");
		nickNameList.add("GeneralLighting4");
		nickNameList.add("GeneralLighting5");
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	public void lightOn() {
		Iterator<String> ite = nickNameList.iterator();
		while (ite.hasNext()) {
			String nickname = ite.next();
			set("[" + nickname + ",[0x80,[0x30]]]", new DeviceListener() {
				@Override
				public void onResponse(JSONObject result) {
					status = LIGHTING_ON;
				}
			});
		}
	}

	public void lightOff() {
		Iterator<String> ite = nickNameList.iterator();
		while (ite.hasNext()) {
			String nickname = ite.next();
			set("[" + nickname + ",[0x80,[0x31]]]", new DeviceListener() {
				@Override
				public void onResponse(JSONObject result) {
					status = LIGHTING_ON;
				}
			});
		}
	}

}
