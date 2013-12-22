package jp.ac.ritsumei.scrambledegg;

import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.scrambledegg.gps.SettingtObjectManager;
import jp.ac.ritsumei.scrambledegg.room.MakeRoomActivity;
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

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends jp.ac.ritsumei.scrambledegg.maps.MapActivity implements OnTouchListener, SensorEventListener{

	/**
	 * 画面下部のフリッパー
	 */
	private ViewFlipper viewFlipper;
    private float posX;

    /**
     * たまご、フライパンの設置を管理
     */
    private jp.ac.ritsumei.scrambledegg.gps.SettingtObjectManager mySettingObjectManager;

    /**
     * たまご、フライパンが設置可能か判定
     */
    private boolean isCanSetObject = false;

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
    private jp.ac.ritsumei.scrambledegg.gps.KeepEggManager myKeepEggManager;

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

    /**
     * フライパンに持ち帰った卵の数
     */
    private int numberOfEggsInFryPan;


    private ExtendApplication app;

    /**
     * 最も近い卵のID
     */
    private int nearestEggId = 0;

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

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		receiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.GET_DATA);
		registerReceiver(receiver, filter);

        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        viewFlipper.setOnTouchListener(this);

        mySettingObjectManager = new jp.ac.ritsumei.scrambledegg.gps.SettingtObjectManager();
        setObjectButton = (Button)findViewById(R.id.setObjectButton);
        setObjectButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.setObjectButton) {
					if(currentState == GAME_STATE.EGG_SET) {
						if(countSetEggs < MAX_SET_EGGS) {
							int eggId = countStayingEgg();
							moveMarker(EGG, eggId, myLocation);
							new postJSONTask().execute(makeEggLocationInfo(myTeam.getEggsList().get(eggId).getEggID(), myLocation));
							countSetEggs++;
						}

					} else if (currentState == GAME_STATE.POSITION_SET) {
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
        
        
        initSensor();
        

        app = (ExtendApplication)getApplication();

        makeRoom();

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
			Log.e("jsondata", gotData);


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
							i--;
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
							for(int i = 0; i < enemyTeam.getPlayersList().size() ; i++) {
								Player member = enemyTeam.getPlayersList().get(i);
								moveMarker(myTeam.getTeamID() + TEAM_A, member.getPlayerID(), new LatLng(member.getLatitude(), member.getLongitude()));
							}
						}
						isLastHaveEgg = true;
					} else {
						if(isLastHaveEgg) {
							hideEnemyMarkers(myTeam.getTeamID(), numberOfTeams);
						}
						isLastHaveEgg = false;
					}
					break;

				case END:
					stopGPSService();
					break;
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
	public JSONObject makeMyLocationInfo(LatLng point){
		JSONObject info = new JSONObject();

		try {
			//先頭にデータの情報をつける
			info.put("DataType", "GPS");
			//更新したいデータの位置
			info.put("roomID", roomID);
			info.put("playerID",myInfo.getPlayerID());
			//更新したい情報
			info.put("latitude", point.latitude);
			info.put("longitude",point.longitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}

//TODO サーバー
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

//TODO サーバー
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
	 * チーム分け
	 */
	public void makeRoom() {
		//TODO 今は仮の設定で、これを事前に決めておかないときちんと更新できないし、ルーム作成で作る
		/**
		 * Roomの中に、Teamがあって、Teamの中にPlayerのリストとEggのリストがある
		 */
		myInfo = new Player("player"+app.getPlayerID(), 1, 35, 135 );

		enemyTeams = new ArrayList<Team>();
		myTeam = new Team("team"+app.getTeamID(),app.getTeamID());
//
//		myTeam.addPlayerList(myInfo);
//		myTeam.addPlayerList(new Player("p2", 2, 35,135));
//		myTeam.addEggList(new Egg(1,35,135));
//		myTeam.addEggList(new Egg(2,35,135));
//
//
//		Team team2 = new Team("team1",2);
//		team2.addPlayerList(new Player("p3",3,35,135));
//		team2.addPlayerList(new Player("p4", 4, 35,135));
//		team2.addEggList(new Egg(3,35,135));
//		team2.addEggList(new Egg(4,35,135));
//		enemyTeams.add(team2);

		numberOfTeams = app.getTeamNum();
		numberOfEggs = app.getPlayerNum();

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
	 * GPSが位置情報を更新した時の処理
	 */
	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		myLocation = new LatLng(location.getLatitude(), location.getLongitude());

		accuracyTextView.setText("" + location.getAccuracy());
		accuracyTextView2.setText("" + location.getAccuracy());
		new postJSONTask().execute(makeMyLocationInfo(myLocation));

		switch(currentState) {
		case LISTEN:
			break;

		case TEAM_SET:
			break;

			/**
			 * フライパン設置時の処理
			 * フライパン設置可能か判定
			 */
		case POSITION_SET:
			if((isCanSetObject = mySettingObjectManager.chaeckGPSAccuracy(location, SettingtObjectManager.ACCURACY_THRESH))){
				gpsAccuracyTextView.setText("GPS:OK");
				setObjectButton.setClickable(true);
			} else {
				gpsAccuracyTextView.setText("GPS:NG");
				setObjectButton.setClickable(false);
			}

			moveMarker(PLAYER, 0, new LatLng(location.getLatitude(), location.getLongitude()));

			break;

			/**
			 * たまご設置時の処理
			 * たまご設置可能か判定
			 */
		case EGG_SET:
			if((isCanSetObject = mySettingObjectManager.chaeckGPSAccuracy(location, SettingtObjectManager.ACCURACY_THRESH))){
				gpsAccuracyTextView.setText("GPS:OK");
				setObjectButton.setClickable(true);
			} else {
				gpsAccuracyTextView.setText("GPS:NG");
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
				Egg egg = myKeepEggManager.getNearestEgg(location, enemyTeams);

				float[] result = new float[3];
				Location.distanceBetween(location.getLatitude(), location.getLongitude(), egg.getLatitude(), egg.getLongitude(), result);

				String directionText = getDirectionString(result[1]);

				distanceTextView.setText(directionText);
				directionTextView.setText(result[0] + "m");

				myKeepEggManager.progressKeepEggBar(location, new LatLng(egg.getLatitude(), egg.getLongitude()));
				if((progress = myKeepEggManager.getProgress()) == MAX_PROGRESS) {
					isCanKeepEgg = true;
					keepEggTextView.setText("YOU CAN GET AN EGG");
					registerAccelerometer();
				} else if((progress = myKeepEggManager.getProgress()) == 0) {
					isCanKeepEgg = false;
					keepEggTextView.setText("YOU ARE NOT NEAR EGGS");
				}else{
					isCanKeepEgg = false;
					keepEggTextView.setText("YOU ARE NEAR AN EGG");
				}
				keepEggProgressBar.setProgress(progress);

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
		viewFlipper.showNext();
		//TODO サーバにたまご保持通知
	}

	public void breakEgg() {
		viewFlipper.showPrevious();
		unregisterAccelerometer();
		//TODO サーバにたまご損失通知
	}

	public void goalEgg() {
		viewFlipper.showPrevious();
		unregisterAccelerometer();
		//TODO サーバにたまご獲得通知
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
	 */
	public int countStayingEgg() {
		int count = 0;
		List<Egg> eggList = myTeam.getEggsList();
		for(int i = 0; i < eggList.size(); i++) {
			if(eggList.get(i).getCurrentEggState() == EGG_STATE.STAY) {
				count++;
			}
		}
		return count;
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
		if(z < -5) keepEgg();
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
			if(isCanKeepEgg) checkMotion(event.values[2]);
		}
	}
	
	/**
	 * 加速度センサー登録解除
	 */
	protected void unregisterAccelerometer(){
		sensorManager.unregisterListener(this);
	}

}