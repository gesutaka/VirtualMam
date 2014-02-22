package com.VirtualMam.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

/**
 * デバイス制御クラス
 * @author 北河
 *
 */
public abstract class DeviceController {

	public static String KADECOT_SERVICE_URI = "http://192.168.1.142:31413/call.json?";

	/**
	 * 家電にset命令を送信する
	 * @param paramString
	 */
	public void set(String paramString, DeviceListener listener) {
		URL url;
		try {
			url = new URL(KADECOT_SERVICE_URI + "method=set&params=" + paramString);
			ControlInfo info = new ControlInfo(url, listener);
			new KadecotReqTask().execute(info);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		};
	}

	public void get(String paramString, DeviceListener listener) {
		URL url;
		try {
			url = new URL(KADECOT_SERVICE_URI + "method=get&params=" + paramString);
			ControlInfo info = new ControlInfo(url, listener);
			new KadecotReqTask().execute(info);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		};
	}

	public abstract int getStatus();
	public abstract void setStatus(int status);


	/**
	 * デバイス制御情報
	 * @author 北河
	 *
	 */
	class ControlInfo {
		private URL url;
		private DeviceListener listener;

		/**
		 * 制御用URIとレスポンス受信時のコールバックを初期化する
		 * @param uri
		 * @param listener
		 */
		public ControlInfo(URL url, DeviceListener listener) {
			this.setUrl(url);
			this.setListener(listener);
		}

		/**
		 * @return listener
		 */
		public DeviceListener getListener() {
			return listener;
		}
		/**
		 * @param listener セットする listener
		 */
		public void setListener(DeviceListener listener) {
			this.listener = listener;
		}
		/**
		 * @return url
		 */
		public URL getUrl() {
			return url;
		}
		/**
		 * @param uri セットする uri
		 */
		public void setUrl(URL url) {
			this.url = url;
		}
	}

	/**
	 * バックグラウンドでkadecotサーバにリクエストする
	 * @author xenon
	 *
	 */
	class KadecotReqTask extends AsyncTask<ControlInfo, Integer, Long> {

		private static final String TAG = "KodecotReqTask";

		@Override
		protected Long doInBackground(ControlInfo... params) {
			ControlInfo info = params[0];
			URL url = info.getUrl();
			DeviceListener listener = info.getListener();
			try {
				Log.d(TAG, "conntect to " + url);
				Object content = url.getContent();
				if (content instanceof InputStream) {
					BufferedReader reader = new BufferedReader(new InputStreamReader( (InputStream)content) );
					StringBuffer buf = new StringBuffer();
				    String str;
				    while ((str = reader.readLine()) != null) {
				            buf.append(str);
				            buf.append("\n");
				    }
				    JSONObject json = new JSONObject(buf.toString());
				    listener.onResponse(json);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

	}
}
