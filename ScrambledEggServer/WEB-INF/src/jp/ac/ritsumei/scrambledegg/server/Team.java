package jp.ac.ritsumei.scrambledegg.server;

import java.util.List;

public class Team {
	
	
	private String teamName;
	private int teamID;
	private double[] position;
	private List<Player> playersList;
	private List<Egg> eggsList;

	
	
	
	
	
	//以下getter setter
	public String getTeamName() {
		return teamName;
	}
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	public int getTeamID() {
		return teamID;
	}
	public void setTeamID(int teamID) {
		this.teamID = teamID;
	}
	public double[] getPosition() {
		return position;
	}
	public void setPosition(double[] position) {
		this.position = position;
	}
	public List<Player> getPlayersList() {
		return playersList;
	}
	public void setPlayersList(List<Player> playersList) {
		this.playersList = playersList;
	}
	public List<Egg> getEggsList() {
		return eggsList;
	}
	public void setEggsList(List<Egg> eggsList) {
		this.eggsList = eggsList;
	}
}
