package jp.ac.ritsumei.scrambledegg.server.gameinfo;

import org.json.JSONException;
import org.json.JSONObject;

public class Player {

	private String name;
	private int playerID;
	private double latitude;
	private double longitude;
	private int teamID;
	private boolean isHaveEgg = false;



	public Player(String name , int pID , double latitude, double longitude) {
		this.name = name;
		this.playerID = pID;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public JSONObject getJSONObject(){
		JSONObject result = new JSONObject();
		try {
			result.put("name", name);
			result.put("playerID", playerID);
			result.put("latitude", latitude);
			result.put("longitude", longitude);
			result.put("teamID", teamID);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPlayerID() {
		return playerID;
	}
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public boolean getIsHaveEgg() {
		return isHaveEgg;
	}

	public void setIsHaveEgg(boolean isHaveEgg) {
		this.isHaveEgg = isHaveEgg;
	}
}
