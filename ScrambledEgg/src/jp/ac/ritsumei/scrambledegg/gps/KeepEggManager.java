package jp.ac.ritsumei.scrambledegg.gps;

import java.util.List;

import jp.ac.ritsumei.scrambledegg.server.gameinfo.Egg;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Egg.EGG_STATE;
import jp.ac.ritsumei.scrambledegg.server.gameinfo.Team;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class KeepEggManager{

	private final double FIRST_DISTANCE_THRESH = 5.0;
	private final double KEEP_DISTANCE_THRESH = 30.0;
	private final double GOAL_DIRECTION_THRESH = 10.0;
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
				}else if(location.getAccuracy() <= 15) {
					progress += 20;
				} else if(location.getAccuracy() <= 30) {
					progress += 10;
				} else if(location.getAccuracy() <= 50) {
					progress += 5;
				}
			} else {
				isKeepNearEgg = false;
				progress = 0;
			}
		}
		
		if(100 <= progress ){
			progress = 100;
		}
	}

	public LatLng getEggPosition(int id) {

		LatLng eggPosition = null;
		//サーバからたまごの位置リストとってくる

		return eggPosition;
	}

	public Egg getNearestEgg(Location location, List<Team> enemyTeams) {

		Egg egg = null;
		double distance = 0;
		float[] results = new float[3];
		boolean isFirst = true;
		distance = results[0];
		for(int team = 0; team < enemyTeams.size(); team++) {
			List<Egg> eggList = enemyTeams.get(team).getEggsList();
			for(int i = 0; i < eggList.size(); i++) {
				if(eggList.get(i).getCurrentEggState() == EGG_STATE.STAY) {
					Location.distanceBetween(location.getLatitude(), location.getLatitude(), eggList.get(i).getLatitude(), eggList.get(i).getLongitude(), results);

					if(isFirst) {
						egg = eggList.get(i);
						distance = results[0];
						isFirst = false;
					} else {
						if(distance > results[0]) {
							egg = eggList.get(i);
							distance = results[0];
						}
					}
				}
			}
		}
		return egg;
	}


	public int getProgress(){
		return progress;
	}
	
	
	
	public void setProgress(int progress) {
		this.progress = progress;
	}

	public boolean isNearGoal(LatLng myPosition, LatLng fryPanPosition) {
		float[] result = new float[3];
		Location.distanceBetween(myPosition.latitude, myPosition.longitude, fryPanPosition.latitude, fryPanPosition.longitude, result);
		if(result[0] < GOAL_DIRECTION_THRESH) {
			return true;
		} else {
			return false;
		}
	}

}
