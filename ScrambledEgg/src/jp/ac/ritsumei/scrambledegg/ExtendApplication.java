package jp.ac.ritsumei.scrambledegg;

import android.app.Application;

/**
 * 情報共有クラス
 * マニュフェストに追加すること
 */
public class ExtendApplication extends Application {
	protected int roomID;
	protected int playerID;
	protected int teamID;
	
	protected int playerNum;
	protected int teamNum;
	
	public int getRoomID(){
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getTeamID() {
		return teamID;
	}

	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

	public int getTeamNum() {
		return teamNum;
	}

	public void setTeamNum(int teamNum) {
		this.teamNum = teamNum;
	}
	
	
}
