package jp.ac.ritsumei.scrambledegg.maps;

import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.scrambledegg.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class MapActivity extends FragmentActivity implements LocationListener{
	protected GoogleMap map;
	protected LocationManager locationManager;
	protected List<MarkerObject> markerList;

	protected static final int PLAYER =1;
	protected static final int FRY_PAN =2;
	protected static final int EGG =3;
	protected static final int TEAM_A =4;
	protected static final int TEAM_B =5;
	protected static final int TEAM_C =6;
	protected static final int TEAM_D =7;
	protected static final int TEAM_E =8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_main);
		markerList = new ArrayList<MarkerObject>();
		setMap();
	}

	/**
	 * Start GPS Logging.
	 * Called when receive start intent from Activity
	 * if there is not GPS Scenario, stop this service
	 */
	protected void startGPSService() {
		// ロケーションマネージャのインスタンスを取得する
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		// 位置情報の更新を受け取るように設定
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // プロバイダ
				0, // 通知のための最小時間間隔
				0, // 通知のための最小距離間隔
				this); // 位置情報リスナー
	}

	/**
	 * Stop GPS Logging.
	 * Called when receive stop intent from Activity
	 */
	protected void stopGPSService() {
		if(locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		moveToLocation(location);
	}

	@Override
	public void onProviderDisabled(String s) {
	}

	@Override
	public void onProviderEnabled(String s) {
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
	}

	protected void setMap(){
		map = ((SupportMapFragment)
		      getSupportFragmentManager().findFragmentById(R.id.map))
		      .getMap();

		try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			Log.d("map", "You must update Google Maps.");
			finish();
		}
		moveToLocation(new LatLng(34.979561, 135.964429));
	}


	protected void moveToLocation(Location location) {

		CameraUpdate cull =
				CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
		map.moveCamera(cull);

	}

	protected void moveToLocation(LatLng latlng) {

		CameraUpdate cull =
				CameraUpdateFactory.newLatLngZoom(latlng, 17);
		map.moveCamera(cull);

	}


	//TODO マーカー管理(プレイヤー、たまご、フライパン)
	protected void createPlayerMarker() {
		createMarker(PLAYER, 0);
	}

	protected void createFryPanMarker() {
		createMarker(FRY_PAN, 0);
	}

	protected void createEggMarkers(int numberOfEggs) {
		for(int i = 0; i < numberOfEggs; i++) {
			createMarker(EGG, i);
		}
	}

	protected void createTeamMarkers(int team, int numberOfMembers) {
		for(int i = 0; i < numberOfMembers; i++) {
			createMarker(team, i);
		}
	}

	protected void createAllMarkers(int numberOfEggs, int numberOfTeams) {

		createPlayerMarker();

		createFryPanMarker();

		createEggMarkers(numberOfEggs);

		for(int i = 0; i < numberOfTeams; i++) {
			createTeamMarkers(i + TEAM_A, numberOfEggs / 2);
		}
	}

	protected int getNumberOfMenbers(int team) {
		return 3;	//チーム人数取得
	}

	protected int getNumberOfTeam(){
		return 2;	//チーム数取得
	}

	/**
	 * マーカーを作成(表示しない)
	 * @param kind
	 * @param id
	 */
	protected void createMarker(int kind, int id) {
		MarkerOptions options = new MarkerOptions();
		options.icon(setIcon(kind));
		options.position(new LatLng(0, 0));
		options.visible(false);

		Marker marker = map.addMarker(options);
		markerList.add(new MarkerObject(kind, id, marker));
	}

	protected  BitmapDescriptor setIcon(int kind) {
		BitmapDescriptor icon = null;
		switch(kind) {
		case PLAYER:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
			break;
		case FRY_PAN:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_fry_pan);
			break;
		case EGG:
			icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_egg);
			break;
		case TEAM_A:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
			break;
		case TEAM_B:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
			break;
		case TEAM_C:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
			break;
		case TEAM_D:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
			break;
		case TEAM_E:
			icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
			break;
		default:
		}
		return icon;
	}

	protected void displayPlayerMarker() {
		displayMarkers(PLAYER);
	}

	protected void displayFryPanMarker() {
		displayMarkers(FRY_PAN);
	}

	protected void displayEggMarkers() {
		displayMarkers(EGG);
	}

	protected void displayEnemyMarkers(int playerTeam, int numberOfTeams) {
		for(int i = 0; i < numberOfTeams; i++) {
			if((i + TEAM_A) != playerTeam) {
				displayMarkers(i + TEAM_A);
			}
		}
	}

	protected void displayTeamMarkers(int team) {
		displayMarkers(team);
	}

	/**
	 * マーカを地図上に表示する
	 * @param kind
	 */
	protected void displayMarkers(int kind) {
		for(int i = 0; i < markerList.size(); i++) {
			if(markerList.get(i).getKind() == kind) {
				markerList.get(i).getMarker().setVisible(true);
			}
		}
	}

	protected void hidePlayerMarker() {
		hideMarkers(PLAYER);
	}

	protected void hideFryPanMarker() {
		hideMarkers(FRY_PAN);
	}

	protected void hideEggMarkers() {
		hideMarkers(EGG);
	}

	protected void hideEnemyMarkers(int playerTeam, int numberOfTeams) {
		for(int i = 0; i < numberOfTeams; i++) {
			if((i + TEAM_A) != playerTeam) {
				hideMarkers(i + TEAM_A);
			}
		}
	}

	protected void hideTeamMarkers(int team) {
		hideMarkers(team);
	}

	/**
	 * マーカを地図上から消す(種類すべて)
	 * @param kind
	 */
	protected void hideMarkers(int kind) {
		for(int i = 0; i < markerList.size(); i++) {
			if(markerList.get(i).getKind() == kind) {
				markerList.get(i).getMarker().setVisible(false);
			}
		}
	}

	protected void hideMarker(int kind, int id) {
		for(int i = 0; i < markerList.size(); i++) {
			if(markerList.get(i).getKind() == kind && markerList.get(i).getId() == id) {
				markerList.get(i).getMarker().setVisible(false);
			}
		}
	}

	/**
	 * マーカーを移動する
	 * @param kind
	 * @param id
	 * @param point
	 */
	protected void moveMarker(int kind, int id, LatLng point) {
		int index = -1;
		for(int i = 0; i < markerList.size(); i++) {
			if (markerList.get(i).getKind() == kind && markerList.get(i).getId() == id) {
				index = i;
				break;
			}
		}

		if(kind != PLAYER) {
			Log.v("Marker", "kind:" + kind + ", id:" + id + ", index:" + index);
			Log.v("Maker", "" + point.latitude + ", " + point.longitude);
		}
		if(index != -1) {
			markerList.get(index).getMarker().remove();
			markerList.get(index).setMarker((map.addMarker(new MarkerOptions().position(point).icon(setIcon(kind)))));
			markerList.get(index).getMarker().setVisible(true);
		}
	}

	protected void setMarker(int kind, int id, LatLng point) {
		int index = -1;
		for(int i = 0; i < markerList.size(); i++) {
			if (markerList.get(i).getKind() == kind && markerList.get(i).getId() == id) {
				index = i;
				break;
			}
		}

		if(index != -1) {
			markerList.get(index).getMarker().setVisible(true);
			markerList.get(index).getMarker().setPosition(point);
		}
	}
}