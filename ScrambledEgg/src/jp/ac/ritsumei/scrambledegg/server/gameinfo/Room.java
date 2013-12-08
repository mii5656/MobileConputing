package jp.ac.ritsumei.scrambledegg.server.gameinfo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Room {

	/**
	 * ゲーム状態
	 */
	public enum GAME_STATE {LISTEN,TEAM_SET,POSITION_SET,EGG_SET,START,END};

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
	
	/**
	 *　チームリスト
	 */
	private List<Team> teamsList;

	

	/**
	 * 初期状態
	 */
	public Room(int roomID) {
		currentState = GAME_STATE.LISTEN;
		teamsList = new ArrayList<Team>();
		this.roomID = roomID;
	}
	


	public void addTeamList(Team t){
		teamsList.add(t);
	}


	public JSONObject getJSONObject(){
		JSONObject result = new JSONObject();
		try {
			result.put("GameState", currentState);
			result.put("roomID", roomID);
			result.put("time", elapsedTime);
			
			
			JSONArray teams = new JSONArray();
			for(Team t : teamsList){
				teams.put(t.getJSONObject());
			}
			result.put("teams", teams);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	public GAME_STATE getCurrentState() {
		return currentState;
	}
	public void setCurrentState(GAME_STATE currentState) {
		this.currentState = currentState;
	}
	public int getRoomID() {
		return roomID;
	}
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}
	public long getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	public List<Team> getTeamsList() {
		return teamsList;
	}
	public void setTeamsList(List<Team> teamsList) {
		this.teamsList = teamsList;
	}
}
