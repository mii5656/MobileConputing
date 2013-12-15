package jp.ac.ritsumei.scrambledegg.server.gameinfo;

import org.json.JSONException;
import org.json.JSONObject;


public class Egg {

	/**
	 * ゲーム状態
	 */
	public enum EGG_STATE {MADE,STAY,CARRYING,BROKEN};

	/**
	 * 現在のゲーム状態
	 */
	private EGG_STATE currentEggState;


	private int eggID;
	private int teamID;
	private double latitude;
	private double longitude;


	/**
	 * コンストラクタ
	 */
	public Egg( int eggID, double latitude,double longitude) {
		this.eggID = eggID;
		this.latitude = latitude;
		this.longitude = longitude;
		this.currentEggState = EGG_STATE.MADE;
	}


	public JSONObject getJSONObject(){
		JSONObject result = new JSONObject();
		try {
			result.put("eggID", eggID);
			result.put("latitude", latitude);
			result.put("longitute", longitude);
			result.put("teamID", teamID);
			result.put("state", currentEggState);
		}catch (JSONException e){
			e.printStackTrace();
		}
		return result;
	}


	//以下getter setter
	public int getEggID() {
		return eggID;
	}


	public void setEggID(int eggID) {
		this.eggID = eggID;
	}


	public int getTeamID() {
		return teamID;
	}


	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}


	public double getLatitude() {
		return latitude;
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public double getLongitude() {
		return longitude;
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public EGG_STATE getCurrentEggState() {
		return currentEggState;
	}


	public void setCurrentEggState(EGG_STATE currentEggState) {
		this.currentEggState = currentEggState;
	}
}
