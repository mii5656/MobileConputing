package jp.ac.ritsumei.scrambledegg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.scrambledegg.gps.KeepEggManager;
import jp.ac.ritsumei.scrambledegg.gps.SettingtObjectManager;
import jp.ac.ritsumei.scrambledegg.room.ResultActivity;
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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends jp.ac.ritsumei.scrambledegg.maps.MapActivity implements OnTouchListener, SensorEventListener{

	/**
	 * 画面下部のフリッパー
	 */
	private ViewFlipper viewFlipper;
	//private float posX;

	/**
	 * たまご、フライパンの設置を管理
	 */
	private SettingtObjectManager mySettingObjectManager;


	/**
	 * たまごを設置した数
	 */
	private int countSetEggs = 0;

	/**
	 * たまごの一人あたりの設置数
	 */
	private final int MAX_SET_EGGS= 2;

	/**
	 * たまごをが取得可能な状態化管理
	 */
	private KeepEggManager myKeepEggManager;

	/**
	 * たまご保持が可能か判定
	 */
	private boolean isCanKeepEgg = false;

	/**
	 * たまご保持のプログレス
	 */
	private final int MAX_PROGRESS =100;
	private ProgressBar keepEggProgressBar;
	private int progress = 0;

	/**
	 * チームの数
	 */
	private int numberOfTeams;

	/**
	 * プレイヤーの最新の位置情報
	 */
	private LatLng myLocation;

	/**
	 * 一チームあたりのたまごの設置数
	 */
	private int numberOfEggs;

	private ExtendApplication app;
	/**
	 * 最も近い卵のID
	 */
	private Egg nearestEgg ;

	private TextView gpsAccuracyTextView;
	private TextView keepEggTextView;
	private TextView directionTextView;
	private TextView distanceTextView;
	private TextView accuracyTextView;
	private TextView accuracyTextView2;
	private TextView stateTextView;

	Button setObjectButton;

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

	private boolean isLastHaveEgg = false;

	/**
	 * センサー関係
	 */
	private SensorManager sensorManager;

	private ImageView keepEggImg, limitCircle;
	private int[] eggImgLocation, circleImgLocation, eggImgFirstLocation;

	/**
	 * アラート画像
	 */
	private ImageView brokenEggImg, gotEggImg;

	DecimalFormat df = new DecimalFormat("#.#");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		viewFlipper.setOnTouchListener(this);

		mySettingObjectManager = new SettingtObjectManager();
		setObjectButton = (Button)findViewById(R.id.setObjectButton);
		setObjectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.setObjectButton) {
					if(currentState == GAME_STATE.EGG_SET) {
						if(countSetEggs < MAX_SET_EGGS) {
							int eggId = countStayingEgg(0);
							moveMarker(EGG, eggId, myLocation);
							Log.e("log", "eggID:"+eggId+"pid:"+myInfo.getPlayerID()+" id,"+myTeam.getEggsList().get(eggId).getEggID());
							new postJSONTask().execute(makeEggLocationInfo(myTeam.getEggsList().get(eggId).getEggID(), myLocation));
							countSetEggs++;
						}
					}else if (currentState == GAME_STATE.POSITION_SET) {
						new postJSONTask().execute(makeFryPanLocationInfo(myLocation));
						Log.v("Marker","marker---" + myLocation.latitude + "," + myLocation.longitude);
						moveMarker(FRY_PAN, 0, myLocation);
					}
				}
			}
		});

		gpsAccuracyTextView = (TextView)findViewById(R.id.gpsAccuracyText);

		myKeepEggManager = new jp.ac.ritsumei.scrambledegg.gps.KeepEggManager();
		keepEggProgressBar = (ProgressBar)findViewById(R.id.keepEggProgressBar);
		keepEggProgressBar.setMax(MAX_PROGRESS); // 水平プログレスバーの最大値を設定
		keepEggTextView = (TextView)findViewById(R.id.keepEggText);

		directionTextView = (TextView)findViewById(R.id.direction2);
		distanceTextView = (TextView)findViewById(R.id.distance2);

		accuracyTextView = (TextView)findViewById(R.id.textView);
		stateTextView = (TextView)findViewById(R.id.textView2);
		accuracyTextView2 = (TextView)findViewById(R.id.textView3);

		keepEggImg = (ImageView)findViewById(R.id.keepEggImg);

		accuracyTextView = (TextView)findViewById(R.id.textView);
		stateTextView = (TextView)findViewById(R.id.textView2);
		accuracyTextView2 = (TextView)findViewById(R.id.textView3);

		limitCircle = (ImageView)findViewById(R.id.limitCircle);
		eggImgLocation = new int[2];
		circleImgLocation = new int[2];
		eggImgFirstLocation = new int[2];

		eggImgFirstLocation[0] = keepEggImg.getLeft() + keepEggImg.getWidth()/2;
		eggImgFirstLocation[1] = keepEggImg.getTop() + keepEggImg.getHeight()/2;

		//TODO 正しい画像に変える
		brokenEggImg = new ImageView(this);
		brokenEggImg.setImageResource(R.drawable.lost);
		//TODO 正しい画像に変える
		gotEggImg = new ImageView(this);
		gotEggImg.setImageResource(R.drawable.get);

		app = (ExtendApplication)getApplication();
		makeRoom();
		initSensor();
	}


	//TODO サービスの始まりと終わりを正しく決める
	@Override
	protected void onResume(){
		super.onResume();
		receiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.GET_DATA);
		registerReceiver(receiver, filter);
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
		stopGPSService();
	}



	/**
	 * Getした情報を受け取る
	 */
	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			GAME_STATE lastState = currentState;

			String gotData =  intent.getExtras().getString(Constants.DATA);
			//Log.e("jsondata", gotData);


			if(gotData != null){
				//ゲーム情報の書き換え
				parseJSON(gotData);

				switch (currentState) {
				case LISTEN:
					break;

				case TEAM_SET:
					break;

				case POSITION_SET:

					if(lastState != GAME_STATE.POSITION_SET) {
						/**
						 * GPSを起動
						 */
						startGPSService();
						//displayPlayerMarker();
						setObjectButton.setText("SET FRY PAN");
						setObjectButton.setClickable(false);
					}
					break;

				case EGG_SET:

					if(lastState != GAME_STATE.EGG_SET) {
						setObjectButton.setText("SET EGG");
						setObjectButton.setClickable(false);
					}
					break;

				case START:

					if(lastState != GAME_STATE.START) {
						viewFlipper.showNext();
						//						displayTeamMarkers(myTeam.getTeamID());
						//						displayFryPanMarker();
						//						displayEggMarkers();
					}

					for(int i = 0; i < myTeam.getEggsList().size(); i++) {
						Egg egg = myTeam.getEggsList().get(i);
						moveMarker(EGG, egg.getEggID(), new LatLng(egg.getLatitude(), egg.getLongitude()));
					}
					moveMarker(FRY_PAN, 0, new LatLng(myTeam.getBaseLatitude(), myTeam.getBaseLongitude()));

					/**
					 * チームメンバーの位置を更新
					 */
					for(int i = 0; i < myTeam.getPlayersList().size() ; i++) {
						Player member = myTeam.getPlayersList().get(i);
						if(member != myInfo) {
							moveMarker(myTeam.getTeamID(), i, new LatLng(member.getLatitude(), member.getLongitude()));
						} else {
							//i--;
						}
					}

					if(myInfo.getIsHaveEgg()) {
						if(!isLastHaveEgg) {
							displayEnemyMarkers(myTeam.getTeamID(), numberOfTeams);
						}
						/**
						 * 敵メンバーの位置を更新
						 */
						for(int team = 0; team < enemyTeams.size(); team++) {
							Team enemyTeam = enemyTeams.get(team);
							for(int i = 0; i < enemyTeam.getPlayersList().size() ; i++){
								Player member = enemyTeam.getPlayersList().get(i);
								moveMarker(enemyTeam.getTeamID() + TEAM_A, member.getPlayerID(), new LatLng(member.getLatitude(), member.getLongitude()));
							}
						}
						isLastHaveEgg = true;
					} else {
						if(isLastHaveEgg){
							hideEnemyMarkers(myTeam.getTeamID(), numberOfTeams);
						}
						isLastHaveEgg = false;
					}
					break;

				case END:
					stopGPSService();
					showResult();
					break;
				}
				Log.i("client", myInfo.getJSONObject().toString());
				//TODO
				Log.i("client", myTeam.getJSONObject().toString());
				for(Team t:enemyTeams){
					Log.i("client", t.getJSONObject().toString());	
				}
			}
		}
	}



	public void parseJSON(String JSONstring){
		try {

			//room情報
			JSONObject object = new JSONObject(JSONstring);
			currentState = GAME_STATE.valueOf( object.getString("GameState"))  ;
			elapsedTime = object.getLong("time");

			stateTextView.setText(""+currentState);

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
					List<Player> pList = myTeam.getPlayersList();
					if(pList.size() == 0){//初期
						for(int j = 0;j < players.length();j++){
							JSONObject player = players.getJSONObject(j);
							if(myInfo.getPlayerID()==player.getInt("playerID")){
								pList.add(new Player("Player"+player.getInt("playerID")
										, player.getInt("playerID")
										, player.getDouble("latitude")
										, player.getDouble("longitude")));
							}
						}
					}else{
						for(int j = 0;j < players.length();j++){
							JSONObject player = players.getJSONObject(j);
							//自分の情報はいらない
							if(myInfo.getPlayerID() != player.getInt("playerID")){
								for(Player p : pList){
									//チームメンバの位置情報更新
									if(player.getInt("playerID") == p.getPlayerID()){
										p.setLatitude(player.getDouble("latitude"));
										p.setLongitude(player.getDouble("longitude"));
									}
								}
							}
						}
					}


					//Eggs List
					JSONArray eggs = team.getJSONArray("eggsList");
					List<Egg> eList = myTeam.getEggsList();
					if(eList.size() == 0){//初期
						for(int j = 0;j < eggs.length();j++){
							JSONObject egg = eggs.getJSONObject(j);
							eList.add(new Egg(egg.getInt("eggID"),
									egg.getDouble("latitude"),
									egg.getDouble("longitude")));
						}

					}else if(eggs.length() <= eList.size()){
						//削除
						List<Egg> deleteList = new ArrayList<Egg>();

						for(Egg e : eList){//現在のリスト
							boolean match = false;

							for(int j = 0;j < eggs.length();j++){//サーバのリスト
								JSONObject egg = eggs.getJSONObject(j);
								if(e.getEggID() == egg.getInt("eggID")){//matchしたら更新
									e.setLatitude(egg.getDouble("latitude"));
									e.setLongitude(egg.getDouble("longitude"));
									e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
									match = true;
									break;
								}
							}

							if(!match){
								deleteList.add(e);
							}
						}

						//delete 
						for(int k=0 ; k<deleteList.size();k++){
							eList.remove(deleteList.get(k));
						}
					}else if(eList.size() <= eggs.length()){

						//追加
						for(int j = 0;j < eggs.length();j++){//サーバのリスト
							boolean match = false;
							JSONObject egg = eggs.getJSONObject(j);

							for(Egg e : eList){//現在のリスト
								if(e.getEggID() == egg.getInt("eggID")){//matchしたら更新
									e.setLatitude(egg.getDouble("latitude"));
									e.setLongitude(egg.getDouble("longitude"));
									e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
									match = true;
									break;
								}
							}
							if(!match){
								Egg tmpEgg = new Egg(egg.getInt("eggID"), egg.getDouble("latitude"), egg.getDouble("longitude"));
								tmpEgg.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
								eList.add(tmpEgg);
							}
						}
					}else{
						for(int j = 0; j < eggs.length(); j++){
							JSONObject egg = eggs.getJSONObject(j);
							for(Egg e : eList){
								if(e.getEggID() == egg.getInt("eggID")){
									e.setLatitude(egg.getDouble("latitude"));
									e.setLongitude(egg.getDouble("longitude"));
									e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
								}
							}
						}
					}




				}else{//相手チーム情報の更新

					boolean ditectTeamflag=true;
					for(Team t : enemyTeams){
						//チームがあるかどうか
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
							List<Egg> eList = t.getEggsList();

							if(eggs.length() <= eList.size()){
								//削除
								List<Egg> deleteList = new ArrayList<Egg>();
								for(Egg e : eList){//現在のリスト
									boolean match = false;

									for(int j = 0;j < eggs.length();j++){//サーバのリスト
										JSONObject egg = eggs.getJSONObject(j);
										if(e.getEggID() == egg.getInt("eggID")){//matchしたら更新
											e.setLatitude(egg.getDouble("latitude"));
											e.setLongitude(egg.getDouble("longitude"));
											e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
											match = true;
											break;
										}
									}
									if(!match){
										deleteList.add(e);
									}
								}

								//delete 
								for(int k=0 ; k<deleteList.size();k++){
									eList.remove(deleteList.get(k));
								}
							}else if(eList.size() <= eggs.length()){
								//追加
								for(int j = 0;j < eggs.length();j++){//サーバのリスト
									boolean match = false;
									JSONObject egg = eggs.getJSONObject(j);

									for(Egg e : t.getEggsList()){//現在のリスト
										if(e.getEggID() == egg.getInt("eggID")){//matchしたら更新
											e.setLatitude(egg.getDouble("latitude"));
											e.setLongitude(egg.getDouble("longitude"));
											e.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
											match = true;
											break;
										}
									}
									if(!match){
										Egg tmpEgg = new Egg(egg.getInt("eggID"), egg.getDouble("latitude"), egg.getDouble("longitude"));
										tmpEgg.setCurrentEggState(EGG_STATE.valueOf(egg.getString("state")));
										eList.add(tmpEgg);
									}
								}
							}else{
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
							}

							ditectTeamflag = false;
						}//teamID
					}

					if(ditectTeamflag){//チームが追加されてない時
						Team t = new Team("グループ"+team.getInt("teamID"), team.getInt("teamID"));
						//基地の更新
						t.setBaseLatitude(team.getDouble("latitude"));
						t.setBaseLongitude(team.getDouble("longitude"));

						//Players List
						JSONArray players = team.getJSONArray("playersList");
						for(int j = 0;j < players.length();j++){
							JSONObject player = players.getJSONObject(j);

							//敵チームメンバ追加
							t.addPlayerList(new Player("Player"+player.getInt("playerID")
									, player.getInt("playerID")
									, player.getDouble("latitude")
									, player.getDouble("longitude")));
						}//player

						//Eggs List
						JSONArray eggs = team.getJSONArray("eggsList");
						for(int j = 0; j < eggs.length(); j++){
							JSONObject egg = eggs.getJSONObject(j);
							t.addEggList(new Egg(egg.getInt("eggID"), egg.getDouble("latitude"), egg.getDouble("longitude")));
						}//egg
						enemyTeams.add(t);
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
	public JSONObject makeMyLocationInfo(LatLng point){
		JSONObject info = new JSONObject();

		try{
			//先頭にデータの情報をつける
			info.put("DataType", "GPS");
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("teamID", myInfo.getTeamID());
			info.put("playerID",myInfo.getPlayerID());
			//更新したい情報
			info.put("latitude", point.latitude);
			info.put("longitude",point.longitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * サーバに送るたまご情報をJSONにする
	 * @return
	 */
	public JSONObject makeEggLocationInfo(int eggId, LatLng point){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "EGG");
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("teamID",myTeam.getTeamID());
			info.put("eggID", eggId);
			//更新したい情報
			info.put("latitude", point.latitude);
			info.put("longitude",point.longitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}


	/**
	 * サーバに送るフライパン情報をJSONにする
	 * @return
	 */
	public JSONObject makeFryPanLocationInfo(LatLng point){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "FRY_PAN");
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("teamID",myTeam.getTeamID());
			//更新したい情報
			info.put("latitude", point.latitude);
			info.put("longitude",point.longitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * サーバに送るたまご保持情報をJSONにする
	 * @return
	 */
	public JSONObject makeEggInfo(String state){
		JSONObject info = new JSONObject();

		try {
			/**
			 * state
			 *  ・KEEP
			 *  ・BREAK
			 *  ・GET
			 */
			//先頭にデータの情報をつける
			info.put("DataType", "EGG_"+state);
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("eggID", nearestEgg.getEggID());
			info.put("teamID", nearestEgg.getTeamID());
			info.put("myTeamID", myTeam.getTeamID());

			info.put("eggLat", nearestEgg.getLatitude());
			info.put("eggLng", nearestEgg.getLongitude());

			Log.e("egg","EGG_"+state);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * チーム分け
	 */
	public void makeRoom() {
		/**
		 * Roomの中に、Teamがあって、Teamの中にPlayerのリストとEggのリストがある
		 */
		roomID = app.getRoomID();

		myInfo = new Player("player"+app.getPlayerID(), app.getPlayerID(), 35, 135 );
		myInfo.setTeamID(app.getTeamID());

		enemyTeams = new ArrayList<Team>();
		myTeam = new Team("team"+app.getTeamID(),app.getTeamID());

		numberOfTeams = app.getTeamNum();
		numberOfEggs = app.getPlayerNum() * 2;

		createAllMarkers(numberOfEggs, numberOfTeams);

	}

	/**
	 * 勝利をサーバに通知する
	 */
	public void notifyWining() {
		//myTeamの勝利をサーバに送信
	}

	/**
	 * 勝利
	 */
	public void win() {

	}

	/**
	 * 敗北
	 */
	public void lose() {

	}

	/**
	 * 引き分け
	 */
	public void draw() {

	}
	/**
	 * GPSが位置情報を更新した時の処理
	 */
	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		myLocation = new LatLng(location.getLatitude(), location.getLongitude());

		accuracyTextView.setText("" + location.getAccuracy());
		accuracyTextView2.setText("" + location.getAccuracy());
		new postJSONTask().execute(makeMyLocationInfo(myLocation));

		switch(currentState){
		case LISTEN:
			break;

		case TEAM_SET:
			break;

			/**
			 * フライパン設置時の処理
			 * フライパン設置可能か判定
			 */
		case POSITION_SET:
			if((mySettingObjectManager.chaeckGPSAccuracy(location, SettingtObjectManager.ACCURACY_THRESH))){
				gpsAccuracyTextView.setText("GPS Accuracy:OK");
				setObjectButton.setClickable(true);
			} else {
				gpsAccuracyTextView.setText("GPS Accuracy:NG");
				setObjectButton.setClickable(false);
			}

			moveMarker(PLAYER, 0, new LatLng(location.getLatitude(), location.getLongitude()));

			break;

			/**
			 * たまご設置時の処理
			 * たまご設置可能か判定
			 */
		case EGG_SET:
			if((mySettingObjectManager.chaeckGPSAccuracy(location, SettingtObjectManager.ACCURACY_THRESH))){
				gpsAccuracyTextView.setText("GPS Accuracy:OK");
				setObjectButton.setClickable(true);
			} else {
				gpsAccuracyTextView.setText("GPS Accuracy:NG");
				setObjectButton.setClickable(false);
			}

			moveMarker(PLAYER, 0, new LatLng(location.getLatitude(), location.getLongitude()));

			break;

			/**
			 * たまご探索中の処理
			 * ・ヒント情報の更新
			 * ・たまご保持可能か判定
			 * ・チームメンバーの位置更新
			 */
		case START:

			moveMarker(PLAYER, 0, new LatLng(location.getLatitude(), location.getLongitude()));

			/**
			 * たまご探索中の処理
			 */
			if(!myInfo.getIsHaveEgg()) {
				if((nearestEgg = myKeepEggManager.getNearestEgg(location, enemyTeams)) != null) {

					float[] result = new float[3];
					Location.distanceBetween(location.getLatitude(), location.getLongitude(), nearestEgg.getLatitude(), nearestEgg.getLongitude(), result);

					String directionText = getDirectionString(result[1]);


					myKeepEggManager.progressKeepEggBar(location, new LatLng(nearestEgg.getLatitude(), nearestEgg.getLongitude()));
					if((progress = myKeepEggManager.getProgress()) == MAX_PROGRESS) {
						isCanKeepEgg = true;
						keepEggTextView.setText("YOU CAN GET AN EGG");
						directionTextView.setText("-----");
						distanceTextView.setText("-----");
						registerAccelerometer();
					} else if((progress = myKeepEggManager.getProgress()) == 0) {
						isCanKeepEgg = false;
						keepEggTextView.setText("YOU ARE NOT NEAR EGGS");
						directionTextView.setText(directionText);
						distanceTextView.setText(df.format(result[0]) + "m");
					}else{
						isCanKeepEgg = false;
						keepEggTextView.setText("YOU ARE NEAR AN EGG");
						directionTextView.setText("-----");
						distanceTextView.setText("-----");
					}
					keepEggProgressBar.setProgress(progress);
				} else {
					keepEggTextView.setText("YOU ARE NOT NEAR AN EGG");
					directionTextView.setText("-----");
					distanceTextView.setText("-----");
				}

			} else {
				if(myKeepEggManager.isNearGoal(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(myTeam.getBaseLatitude(), myTeam.getBaseLongitude()))) {
					gotEgg();
				}
			}

			break;

			/**
			 * 結果画面
			 */
		case END:

			break;
		}
	}


	public void keepEgg() {
		Log.v("Egg", "keep egg");
		myInfo.setIsHaveEgg(true);
		isCanKeepEgg = false;
		eggImgLocation[0] = eggImgFirstLocation[0];
		eggImgLocation[1] = eggImgFirstLocation[1];


		viewFlipper.showNext();
		myKeepEggManager.setProgress(0);

		//サーバにたまご保持通知
		new postJSONTask().execute(makeEggInfo("KEEP"));

		//画面にトースト表示
		//Toast.makeText(this, "!!たまご保持中!!", Toast.LENGTH_LONG).show();
	}

	public void breakEgg() {
		Log.v("Egg", "Break egg" +"lat:"+circleImgLocation[0]+" lng: "+circleImgLocation[1]);
		myInfo.setIsHaveEgg(false);
		viewFlipper.showPrevious();
		unregisterAccelerometer();

		keepEggProgressBar.setProgress(0);
		//サーバにたまご損失通知
		new postJSONTask().execute(makeEggInfo("BREAK"));

		//たまご消失アラート表示
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("消失")
		.setView(brokenEggImg)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();
	}

	public void gotEgg() {
		Log.v("Egg", "Goal egg");
		myInfo.setIsHaveEgg(false);
		viewFlipper.showPrevious();
		unregisterAccelerometer();
		//サーバにたまご獲得通知
		new postJSONTask().execute(makeEggInfo("GET"));

		//たまご獲得アラート表示
//		new AlertDialog.Builder(MainActivity.this)
//		.setTitle("獲得")
//		.setView(gotEggImg)
//		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//			}
//		})
//		.show();
	}

	public String getDirectionString(double direction) {
		if(-45 <= direction && direction < 45) {
			return "NORTH";
		} else if (45 <= direction && direction < 135) {
			return "EAST";
		}  else if(-135 <= direction && direction < 45) {
			return "WEST";
		} else {
			return "SOUTH";
		}
	}

	/**
	 *  タッチイベント発生時
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		//たまごを保持している状態で画面がタッチされたらたまごを壊す
		if(event.getAction() == MotionEvent.ACTION_DOWN && myInfo.getIsHaveEgg()) breakEgg();

		//		/**
		//		 * 画面下部のフリップ動作
		//		 */
		//		switch(event.getAction()){
		//
		//		case MotionEvent.ACTION_DOWN:
		//			// タッチ場所を取得
		//			posX = event.getX();
		//			break;
		//		case MotionEvent.ACTION_UP:
		//			if(posX > event.getX()){
		//
		//
		//				// 次ページへ移動
		//				viewFlipper.showNext();
		//			}else if(posX < event.getX()){
		//
		//				// 前ページへ移動
		//				viewFlipper.showPrevious();
		//			}
		//		default:
		//			break;
		//		}
		return true;
	}

	//	@Override
	//	public void onClick(View v) {
	//		if (v == setObjectButton) {
	//			Log.v("TEST","tap");
	//			System.out.println("tap");
	//			if(currentState == GAME_STATE.EGG_SET) {
	//				if(countSetEggs < MAX_SET_EGGS) {
	//					int eggId = countStayingEgg();
	//					new postJSONTask().execute(makeEggLocationInfo(eggId, myLocation));
	//					moveMarker(EGG, eggId, myLocation);
	//					countSetEggs++;
	//				}
	//
	//			} else if (currentState == GAME_STATE.POSITION_SET) {
	//				new postJSONTask().execute(makeFryPanLocationInfo(myLocation));
	//				Log.v("TEST","marker");
	//				System.out.println("marker");
	//				moveMarker(FRY_PAN, 0, myLocation);
	//			}
	//		}
	//	}

	/**
	 * 設置されているたまごの数を数える
	 * teamIdが0のときmyTeam
	 * 1のときenemyTeam
	 */
	public int countStayingEgg(int teamId) {
		int count = 0;
		Team team = null;
		if(teamId == 0) {
			team = myTeam;
		} else if(teamId == 1){
			team = enemyTeams.get(0);
		}
		List<Egg> eggList = team.getEggsList();
		for(int i = 0; i < eggList.size(); i++) {
			if(eggList.get(i).getCurrentEggState() == EGG_STATE.STAY) {
				count++;
			}
		}
		return count;
	}

	public void showResult() {
		int myFinalEggs = countStayingEgg(0);
		int enemyFinalEggs = countStayingEgg(1);

		Intent intent = new Intent(getApplication(), ResultActivity.class);
		intent.putExtra("myFinalEggs", myFinalEggs);
		intent.putExtra("enemyFinalEggs", enemyFinalEggs);
		startActivity(intent);
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

	/**
	 * たまご獲得動作検出
	 */
	private void checkMotion(float z){

		//Log.v("CheckMotion", "acc[2]:" + z);
		if(z < -5) keepEgg();
	}

	/**
	 * たまご画像位置更新
	 */
	private void replaceEggImg(float x, float y){
		keepEggImg.layout(keepEggImg.getLeft()-(int)x*3, keepEggImg.getTop()+(int)y*3
				, keepEggImg.getLeft()+ keepEggImg.getWidth()-(int)x*3, keepEggImg.getTop()+keepEggImg.getHeight()+(int)y*3);

		eggImgLocation[0] = keepEggImg.getLeft() + keepEggImg.getWidth()/2;
		eggImgLocation[1] = keepEggImg.getTop() + keepEggImg.getHeight()/2;

		circleImgLocation[0] = limitCircle.getLeft() + limitCircle.getWidth()/2;
		circleImgLocation[1] = limitCircle.getTop() + limitCircle.getHeight()/2;

		//		Log.v("Egg", "get:"+keepEggImg.getWidth() +","+keepEggImg.getHeight());
		//		Log.v("Egg","log image"+eggImgLocation[0]+", "+eggImgLocation[1]+",circle "+circleImgLocation[0]+"."+circleImgLocation[1]);
		//たまごの中心からのずれ計算、損失判定
		double dx = Math.pow((double)eggImgLocation[0]-(double)circleImgLocation[0],2.0);
		double dy = Math.pow((double)eggImgLocation[1]-(double)circleImgLocation[1],2.0);
		if(Math.sqrt(dx+dy) > limitCircle.getWidth()/2) breakEgg();	
	}

	/**
	 * センサー初期化
	 */
	protected void initSensor(){
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	}

	/**
	 * 加速度センサー登録
	 */
	protected void registerAccelerometer(){
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size() > 0){

			Log.v("RegisterAccelerometer", "regist acc");
			Sensor s = sensors.get(0);
			sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/**
	 * センサーイベントハンドラ（精度変化）
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	/**
	 * センサーイベントハンドラ（値変化）
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			//Log.v("OnSensorChanged", "get acc");
			if(isCanKeepEgg) checkMotion(event.values[2]);
			else if(myInfo.getIsHaveEgg()) replaceEggImg(event.values[0],event.values[1]);
		}
	}

	/**
	 * 加速度センサー登録解除
	 */
	protected void unregisterAccelerometer(){
		sensorManager.unregisterListener(this);
	}

	//	protected boolean checkStateUpDate(GAME_STATE gameState){
	//
	//		switch (gameState) {
	//		case LISTEN:
	//			break;
	//		case TEAM_SET:
	//			break;
	//		case POSITION_SET:
	//			break;
	//		case EGG_SET:
	//			break;
	//		case END:
	//			break;
	//		case START:
	//			break;
	//		}
	//		return true;
	//	}
}