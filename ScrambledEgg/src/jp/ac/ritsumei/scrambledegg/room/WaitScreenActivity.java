package jp.ac.ritsumei.scrambledegg.room;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import jp.ac.ritsumei.scrambledegg.Constants;
import jp.ac.ritsumei.scrambledegg.ExtendApplication;
import jp.ac.ritsumei.scrambledegg.MainActivity;
import jp.ac.ritsumei.scrambledegg.R;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Room;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class WaitScreenActivity extends Activity{

	/**
	 * 情報共有
	 */
	private ExtendApplication app;
	
	private int roomID;

	private TextView playerIDtext;
	private TextView teamIDtext;
	
	private Timer infoGetTimer;
	
	/** Called when the activity is first created.*/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wait_other_player);

		app= (ExtendApplication)getApplication();
		roomID=app.getRoomID();
		
		playerIDtext = (TextView)findViewById(R.id.edName);
		teamIDtext = (TextView)findViewById(R.id.edMail);
		
		new postJSONTask().execute(makeRegistterInfo());
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		GetInfoTimerTask task = new GetInfoTimerTask();
		infoGetTimer = new Timer();
		infoGetTimer.schedule(task, 1000, 5000);//5秒サイクルで実行	
	}




	/**
	 * サーバに送る登録情報をJSONにする
	 * @return
	 */
	public JSONObject makeRegistterInfo(){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "Register_Request");
			//更新したい情報
			info.put("roomID", roomID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	
	/**
	 * post用の非同期タスク
	 * 送りたいJSONを作り、渡す
	 */
	private class postJSONTask extends AsyncTask<JSONObject, Integer, Integer> {
		
		@Override
		protected Integer doInBackground(JSONObject... contents) {

			HttpClient httpClient  = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(Constants.URI);

			ArrayList <NameValuePair> params = new ArrayList <NameValuePair>();
			params.add( new BasicNameValuePair("params", contents[0].toString()));

			HttpResponse httpResponse = null;
			try {
				postRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

				httpResponse = httpClient.execute(postRequest);
				
				JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
				Log.e("wait", result.toString());
				if(result.getBoolean("result")){
					app.setPlayerID(result.getInt("playerID"));
					playerIDtext.setText(String.valueOf(app.getPlayerID()));
					app.setTeamID(result.getInt("teamID"));
					teamIDtext.setText(String.valueOf(app.getPlayerID()));
				}else{
					Intent intent = new Intent(app, EntryRoomActivity.class);
					startActivity(intent);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("PostTask", "Error Execute");
			}

			httpClient.getConnectionManager().shutdown();
			return 1;
		}
	}
	
	
	
	/**
	 * サーバに送る登録情報をJSONにする
	 * @return
	 */
	public JSONObject makeStateInfoRequest(){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "State_Request");
			
			//更新したい情報
			info.put("roomID", roomID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	private class GetInfoTimerTask extends TimerTask{
		boolean goNext=false;
		
		@Override
		public void run() {
			HttpClient httpClient  = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(Constants.URI);

			ArrayList <NameValuePair> params = new ArrayList <NameValuePair>();
			params.add( new BasicNameValuePair("params", makeStateInfoRequest().toString()));

			HttpResponse httpResponse = null;
			try {
				postRequest.setEntity(new UrlEncodedFormEntity(params, "utf-8"));

				httpResponse = httpClient.execute(postRequest);
				
				JSONObject result = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
			
			Log.e("wait", ""+result.get("state"));	
				if(Room.GAME_STATE.POSITION_SET.toString().equals(result.get("state").toString())){
					Log.e("wait", ""+result.get("state"));	
					Intent intent = new Intent(app, MainActivity.class);
					startActivity(intent);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("PostTask", "Error Execute");
			}

			httpClient.getConnectionManager().shutdown();
		}	
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		infoGetTimer.cancel();
		super.onPause();
	}
}
