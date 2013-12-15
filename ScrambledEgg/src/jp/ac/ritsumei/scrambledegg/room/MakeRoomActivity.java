package jp.ac.ritsumei.scrambledegg.room;

import java.util.ArrayList;

import jp.ac.ritsumei.scrambledegg.Constants;
import jp.ac.ritsumei.scrambledegg.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class MakeRoomActivity extends Activity{

	private int playerNum = 2;
	private int teamNum = 2;	 


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.makeroom);

		
		//playerSpiner
		ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		
		// アイテムを追加します
		playerAdapter.add("2");
		playerAdapter.add("3");
		playerAdapter.add("4");
		playerAdapter.add("5");
		playerAdapter.add("6");
		playerAdapter.add("7");
		playerAdapter.add("8");
		
		playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner playerSpinner = (Spinner) findViewById(R.id.spinnerPlayer);
		// アダプターを設定します
		playerSpinner.setAdapter(playerAdapter);
		// スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		playerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Spinner spinner = (Spinner) parent;
				// 選択されたアイテムを取得します
				String item = (String) spinner.getSelectedItem();
				playerNum = Integer.parseInt(item);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});



		//playerSpiner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// アイテムを追加します
		adapter.add("2");
		adapter.add("3");
		adapter.add("4");
		adapter.add("5");
		adapter.add("6");
		adapter.add("7");
		adapter.add("8");

		Spinner teamSpinner = (Spinner) findViewById(R.id.spinnerTeam);
		// アダプターを設定します
		teamSpinner.setAdapter(adapter);
		// スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		teamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Spinner spinner = (Spinner) parent;
				// 選択されたアイテムを取得します
				String item = (String) spinner.getSelectedItem();
				teamNum = Integer.parseInt(item);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		Button button1 = (Button)findViewById(R.id.button);
		button1.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.button) {
					if(teamNum <= playerNum){
						Toast.makeText(MakeRoomActivity.this,
								"ルームを作成しています", Toast.LENGTH_LONG
								).show();
						new postJSONTask(getApplicationContext()).execute(makeRoomInfo());
					}else{
						// 通知ダイアログを表示
						Toast.makeText(MakeRoomActivity.this,
								"チーム数 <= 人数　にしてください"+playerNum+","+teamNum, Toast.LENGTH_LONG
								).show();
					}
				}
			}
		});
	}


	/**
	 * post用の非同期タスク
	 * 送りたいJSONを作り、渡す
	 */
	private class postJSONTask extends AsyncTask<JSONObject, Integer, Integer> {
		
		Context appContext;
		
		public postJSONTask(Context c) {
			appContext = c;
		}
		
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

			} catch (Exception e) {
				e.printStackTrace();
				Log.d("HttpSampleActivity", "Error Execute");
			}

			httpClient.getConnectionManager().shutdown();
			return 1;
		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Integer result) {
			Intent intent = new Intent(appContext, EntryRoomActivity.class);
			startActivity(intent);
			super.onPostExecute(result);
		}
	}


	/**
	 * サーバに送るGPS情報をJSONにする
	 * @return
	 */
	public JSONObject makeRoomInfo(){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "MAKE_ROOM");
			
			//更新したい情報
			info.put("playerNum", playerNum);
			info.put("teamNum",teamNum);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}
}
