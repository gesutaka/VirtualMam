package com.VirtualMam.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// タイトルを非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// splash.xmlをViewに指定
		setContentView(R.layout.splash);
		Handler hdl = new Handler();
		// 1000ms遅延させてsplashHandlerを実行
		hdl.postDelayed(new splashHandler(), 1000);
	}
	class splashHandler implements Runnable {
		public void run() {
			// スプラッシュ完了後に実行するActivityを指定
			Intent intent = new Intent(getApplication(), MainActivity.class);
			startActivity(intent);
			// SplashActivityを終了
			SplashActivity.this.finish();
		}
	}
}