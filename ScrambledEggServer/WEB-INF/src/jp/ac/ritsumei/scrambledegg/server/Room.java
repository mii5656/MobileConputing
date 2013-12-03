package jp.ac.ritsumei.scrambledegg.server;

import java.util.List;

public class Room {

	public enum GAME_STATE {LISTEN,TEAM_SET,POSITION_SET,EGG_SET,START,END};
	
	private GAME_STATE currentState;
	private int roomID;
	private long elapsedTime;
	private List<Team> teamsList;
	
	
	
	
	
	
	
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
