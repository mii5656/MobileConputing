package jp.ac.ritsumei.scrambledegg;

import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.scrambledegg.server.gameinfo.Egg;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Egg.EGG_STATE;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Player;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Room.GAME_STATE;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Team;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {


	/**
	 * 定期的にGET情報を受け取る
	 */
	private MyBroadcastReceiver receiver;


	/**
	 * 自分の情報
	 */
	private Player myInfo;

	/**
	 * 自分の所属しているチーム
	 */
	private Team myTeam;

	/**
	 * 敵のチーム
	 */
	private List<Team> enemyTeams;


	/**
	 * 現在のゲーム状態
	 */
	private GAME_STATE currentState;

	/**
	 * ルームID
	 */
	private int roomID;

	/**
	 * 経過時間
	 */
	private long elapsedTime;






	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
//test
		receiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.GET_DATA);
		registerReceiver(receiver, filter);


		//TODO 今は仮の設定で、これを事前に決めておかないときちんと更新できないし、ルーム作成で作る
		/**
		 * Roomの中に、Teamがあって、Teamの中にPlayerのリストとEggのリストがある
		 */
		myInfo = new Player("p1", 1, 35, 135 );
		enemyTeams = new ArrayList<Team>();

		myTeam = new Team("team1",1);

		myTeam.addPlayerList(myInfo);
		myTeam.addPlayerList(new Player("p2", 2, 35,135));
		myTeam.addEggList(new Egg(1,35,135));
		myTeam.addEggList(new Egg(2,35,135));


		Team team2 = new Team("team1",2);
		team2.addPlayerList(new Player("p3",3,35,135));
		team2.addPlayerList(new Player("p4", 4, 35,135));
		team2.addEggList(new Egg(3,35,135));
		team2.addEggList(new Egg(4,35,135));
		enemyTeams.add(team2);
	}


	//TODO サービスの始まりと終わりを正しく決める
	@Override
	protected void onResume(){
		super.onResume();
		//TODO ルームIDを送信しておく必要がある
		startService(new Intent(MainActivity.this, GameInfoGetterService.class));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(receiver != null){
			unregisterReceiver(receiver);
		}
		stopService(new Intent(getApplicationContext(),GameInfoGetterService.class));
	}



	/**
	 * Getした情報を受け取る
	 */
	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String gotData =  intent.getExtras().getString(Constants.DATA);
			Log.e("jsondata", gotData);
			if(gotData != null){
				//ゲーム情報の書き換え
				parseJSON(gotData);
			}
			new postJSONTask().execute(makeGPSInfo());
		}
	}



	public void parseJSON(String JSONstring){
		try {
			//room情報
			JSONObject object = new JSONObject(JSONstring);
			currentState = GAME_STATE.valueOf( object.getString("GameState"))  ;
			elapsedTime = object.getLong("time");

			//team情報
			JSONArray teamArray = object.getJSONArray("teams");
			for(int i = 0;i < teamArray.length();i++){

				JSONObject team = teamArray.getJSONObject(i);
				//自分のチーム情報更新
				if(myInfo.getTeamID() == team.getInt("teamID")){
					myTeam.setBaseLatitude(team.getDouble("latitude"));
					myTeam.setBaseLongitude(team.getDouble("longitude"));

					//Players List
					JSONArray players = team.getJSONArray("playersList");
					for(int j = 0;j < players.length();j++){
						JSONObject player = players.getJSONObject(j);
						//自分の情報はいらない
						if(myInfo.getPlayerID() != player.getInt("playerID")){
							for(Player p : myTeam.getPlayersList()){
								//チームメンバの位置情報更新
								if(player.getInt("playerID") == p.getPlayerID()){
									p.setLatitude(player.getDouble("latitude"));
									p.setLongitude(player.getDouble("longitude"));
								}
							}
						}
					}

					//Eggs List
					JSONArray eggs = team.getJSONArray("eggsList");
					for(int j = 0; j < eggs.length(); j++){
						JSONObject egg = eggs.getJSONObject(j);
						for(Egg e : myTeam.getEggsList()){
							if(e.getEggID() == egg.getInt("eggID")){
								e.setLatitude(egg.getDouble("latitude"));
								e.setLongitude(egg.getDouble("longitude"));
								e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
							}
						}
					}
				}else{//相手チーム情報の更新

					for(Team t : enemyTeams){
						if(t.getTeamID() ==  team.getInt("teamID")){

							//基地の更新
							t.setBaseLatitude(team.getDouble("latitude"));
							t.setBaseLongitude(team.getDouble("longitude"));

							//Players List
							JSONArray players = team.getJSONArray("playersList");
							for(int j = 0;j < players.length();j++){
								JSONObject player = players.getJSONObject(j);

								for(Player p : t.getPlayersList()){
									//敵チームメンバの位置情報更新
									if(player.getInt("playerID") == p.getPlayerID()){
										p.setLatitude(player.getDouble("latitude"));
										p.setLongitude(player.getDouble("longitude"));
									}
								}
							}//player

							//Eggs List
							JSONArray eggs = team.getJSONArray("eggsList");
							for(int j = 0; j < eggs.length(); j++){
								JSONObject egg = eggs.getJSONObject(j);
								for(Egg e : t.getEggsList()){
									if(e.getEggID() == egg.getInt("eggID")){
										e.setLatitude(egg.getDouble("latitude"));
										e.setLongitude(egg.getDouble("longitude"));
										e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
									}	
								}//egg
							}
						}//teamID
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

				Log.i("egg",""+httpResponse.getStatusLine().getStatusCode()+"  : "+EntityUtils.toString(httpResponse.getEntity()));
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("HttpSampleActivity", "Error Execute");
			}

			httpClient.getConnectionManager().shutdown();
			return 1;
		}
	}


	/**
	 * サーバに送るGPS情報をJSONにする
	 * @return
	 */
	public JSONObject makeGPSInfo(){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "GPS");
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("playerID",myInfo.getPlayerID());
			//更新したい情報
			info.put("latitude", myInfo.getLatitude());
			info.put("longitude",myInfo.getLongitude());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}








	//以下 getter setter
	public List<Team> getEnemyTeams() {
		return enemyTeams;
	}



	public void setEnemyTeams(List<Team> enemyTeams) {
		this.enemyTeams = enemyTeams;
	}



	public GAME_STATE getCurrentState() {
		return currentState;
	}



	public void setCurrentState(GAME_STATE currentState) {
		this.currentState = currentState;
	}


	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}



	public int getRoomID() {
		return roomID;
	}



	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}
}