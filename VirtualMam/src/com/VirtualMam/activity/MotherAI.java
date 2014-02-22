package com.VirtualMam.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MotherAI {

	private final String TAG = "MotherAI";

	private Context context;
	private boolean atHome;
	private boolean onTV;
	private BathController bathCnt;
	private LightingController lightingCnt;
	private MWOven mwOven;
//	private int washingStatus;
//	private int mealStatus;
//	private int bathStatus;
//	private int lightingStatus;
//
//	/** 洗濯ステータス 洗濯してない */
//	private final int WASHING_NO = 0;
//	/** 洗濯ステータス 洗濯中 */
//	private final int WASHING_NOW = 1;
//	/** 洗濯ステータス 洗濯完了 */
//	private final int WASHING_FINISHED = 2;
//
//	/** 食事まだ */
//	private final int MEAL_NO = 0;
//	/** 食事中 */
//	private final int MEAL_NOW = 1;
//	/** 食事完了 */
//	private final int MEAL_FINISHED = 2;

//	/** ライト消えてる状態 */
//	private final int LIGHTING_OFF = 0;
//
//	/** ライトついてる状態 */
//	private final int LIGHTING_ON = 1;

	public MotherAI(Context context) {
		this.context = context;
		setAtHome(false);
		setOnTV(false);
		bathCnt = new BathController(context);
		lightingCnt = new LightingController();
		mwOven = new MWOven();
	}

	/**
	 * @return atHome
	 */
	public boolean isAtHome() {
		return atHome;
	}

	/**
	 * @param atHome セットする atHome
	 */
	public void setAtHome(boolean atHome) {
		this.atHome = atHome;
		
		if(atHome) {
			// スマートウォッチに通知
			Intent i = new Intent(context, ExtendService.class);
			i.setAction(ExtendService.INTENT_ACTION_NOTIFY);
			i.putExtra(ExtendService.EXTEND_KEY, "おかえり");
			context.startService(i);
		}
		
		executeMother();
	}

	/**
	 * @return onTV
	 */
	public boolean isOnTV() {
		return onTV;
	}

	/**
	 * @param onTV セットする onTV
	 */
	public void setOnTV(boolean onTV) {
		this.onTV = onTV;
		executeMother();
	}

	/**
	 * AI思考実行
	 */
	private void executeMother() {
		if (atHome == false) {
			return;
		}

		// お風呂制御
		if (onTV) {
			// お風呂制御
			switch (bathCnt.getStatus()) {
			case BathController.BATH_NO_YUHARI:
				// お湯張り開始
				bathCnt.startYuhari();
				break;

			case BathController.BATH_READY:
				if (onTV == false) {
					bathCnt.setStatus(BathController.BATH_IN_BATH);
				}
				break;

			case BathController.BATH_IN_BATH:
				if (onTV == true) {
					bathCnt.setStatus(BathController.BATH_FINISHED);
				}
				break;

			case BathController.BATH_NOW_YUHARI:
				break;

			case BathController.BATH_FINISHED:
				break;

			default:
				Log.e(TAG, "bad status");
			}
		}

		// ライト制御
		if (onTV) {
			// テレビの前に座ったときのアクション
			lightingCnt.lightOff();

		} else {
			// テレビの前から離れたときのアクション
			lightingCnt.lightOn();
		}
	}
}
