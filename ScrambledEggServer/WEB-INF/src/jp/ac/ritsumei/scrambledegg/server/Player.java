package jp.ac.ritsumei.scrambledegg.server;

public class Player {

	private String name;
	private int playerID;
	private double[] position;
	
	
	
	
	public double[] getPosition() {
		return position;
	}
	public void setPosition(double[] position) {
		this.position = position;
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
}
