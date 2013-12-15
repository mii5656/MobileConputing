package jp.ac.ritsumei.scrambledegg.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.ritsumei.scrambledegg.Constants;
import jp.ac.ritsumei.scrambledegg.ExtendApplication;
import jp.ac.ritsumei.scrambledegg.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;



public class EntryRoomActivity extends Activity{
	ListView lv;

	private HttpClient httpClient;
	private HttpGet request;
	private HttpResponse httpResponse;

	private int roomID = -1;
	
	private ExtendApplication app;
	
	private Map<Integer, Map<String,Integer>> viewMap;

	static List<String> items = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	ListView listView1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entry);

		// 追加するアイテムを生成する

		adapter = new ArrayAdapter<String>(
				this,
				R.layout.row,
				R.id.row_textview1,
				items);

		// リストビューにアイテム (adapter) を追加
		ListView listView1 = (ListView)findViewById(R.id.listView1);
		listView1.setAdapter(adapter);

		// アイテムクリック時ののイベントを追加
		listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,
					View view, int pos, long id) {

				// 選択アイテムを取得
				ListView listView = (ListView)parent;
				String item = (String)listView.getItemAtPosition(pos);
				
				Map<String,Integer> data =  viewMap.get(pos);
				app.setRoomID(data.get("roomID"));
				app.setPlayerNum(data.get("playerNum"));
				app.setTeamNum(data.get("teamNum"));
				
				startActivity(new Intent(app,WaitScreenActivity.class));
			}
		});	

		Button button1 = (Button)findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.button1) {
					Intent intent = new Intent(getApplication(), MakeRoomActivity.class);
					startActivity(intent);
				} 
			}
		});
		
		app = (ExtendApplication)getApplication();
		
		viewMap = new HashMap<Integer, Map<String,Integer>>();
		new GetJSONTask().execute((new JSONObject()));
	}



	private class GetJSONTask extends AsyncTask<JSONObject, Integer, Integer> {

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			adapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

		@Override
		protected Integer doInBackground(JSONObject... contents) {

			httpResponse = null;
			try {

				adapter.clear();

				httpClient  = new DefaultHttpClient();	

				//パラメータの設定(URLに付加する)
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("roomID", ""+roomID));
				String query = URLEncodedUtils.format(params, "UTF-8");

				request = new HttpGet(Constants.URI+"?"+query);

				httpResponse = httpClient.execute(request);
	
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){

					JSONArray resultsJSON = new JSONArray(EntityUtils.toString(httpResponse.getEntity()));

					//room名
					for(int i=0; i<resultsJSON.length() ;i++){
						JSONObject resultObj = resultsJSON.getJSONObject(i);
						items.add("Room"+resultObj.getString("roomID")+" : "+resultObj.getString("currentPlayerNum")+"/"+resultObj.getString("playerNum")+"人 "+resultObj.getString("teamNum")+"チーム");
						Map<String,Integer> data = new HashMap<String, Integer>();
						data.put("roomID",resultObj.getInt("roomID"));
						data.put("playerNum",resultObj.getInt("playerNum"));
						data.put("teamNum",resultObj.getInt("teamNum"));
						viewMap.put(i, data);
					}
				}else{
					Log.e("service", "network err :"+ httpResponse.getStatusLine().getStatusCode());
				}
				httpClient.getConnectionManager().shutdown();
			}catch (Exception e){
				e.printStackTrace();
				Log.d("HttpSampleActivity", "Error Execute");
			}
			return 1;
		}
	}

}
