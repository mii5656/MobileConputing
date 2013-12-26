package jp.ac.ritsumei.scrambledegg.gps;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

public class SettingtObjectManager {
	public static final double ACCURACY_THRESH = 35.0;
	public static final int EGG = 1;
	public static final int FRY_PAN = 2;
	private int accuracyCount = 0;
	public SettingtObjectManager(){

	}

	/**
	 * GPSのAccuracyが3回以閾値以上かチェックする
	 * @param location
	 * @param accuracyThresh
	 * @return
	 */
	public boolean chaeckGPSAccuracy(Location location, double accuracyThresh) {
		if(location.getAccuracy() <= accuracyThresh) {
			accuracyCount++;
			if(accuracyCount >= 3) {
				return true;
			} else {
				return false;
			}
		} else {
			accuracyCount = 0;
			return false;
		}
	}

	/**
	 * チーム内で全てのたまごが設置されたか判定する
	 */
	public boolean isSetEggMax() {
		//サーバから確認
		return false;
	}
}

