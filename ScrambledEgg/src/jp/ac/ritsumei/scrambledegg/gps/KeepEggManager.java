package jp.ac.ritsumei.scrambledegg.gps;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class KeepEggManager {

	private final double FIRST_DISTANCE_THRESH = 5.0;
	private final double KEEP_DISTANCE_THRESH = 30.0;
	private int progress = 0;
	private boolean isKeepNearEgg = false;

	public KeepEggManager() {
	}

	public boolean isNearEgg(LatLng myPosition, LatLng eggPosition, double thresh) {
		float[] result = new float[3];
		Location.distanceBetween(myPosition.latitude, myPosition.longitude, eggPosition.latitude, eggPosition.longitude, result);
		if(result[0] < thresh) {
			return true;
		} else {
			return false;
		}
	}

	public void progressKeepEggBar(Location location, LatLng eggPosition) {
		LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());

		if(!isKeepNearEgg) {
			if(isNearEgg(myPosition, eggPosition, FIRST_DISTANCE_THRESH)) {
				isKeepNearEgg = true;
				if(location.getAccuracy() == 0) {
				}else if(location.getAccuracy() < 15) {
					progress += 20;
				} else if(location.getAccuracy() < 30) {
					progress += 10;
				} else if(location.getAccuracy() < 50) {
					progress += 5;
				} else {
					isKeepNearEgg = false;
				}
			}
		} else {
			if(isNearEgg(myPosition, eggPosition, KEEP_DISTANCE_THRESH)) {
				if(location.getAccuracy() == 0) {
				}else if(location.getAccuracy() < 15) {
					progress += 20;
				} else if(location.getAccuracy() < 30) {
					progress += 10;
				} else if(location.getAccuracy() < 50) {
					progress += 5;
				}
			} else {
				isKeepNearEgg = false;
				progress = 0;
			}
		}
	}

	public LatLng getEggPosition(int id) {

		LatLng eggPosition = null;
		//サーバからたまごの位置リストとってくる

		return eggPosition;
	}

	public int getNearestEggId(Location location) {

		LatLng eggPosition = null;
		//サーバからたまごの位置リストとってくる
		int id = 0;
		return id;
	}


	public int getProgress(){
		return progress;
	}
}
