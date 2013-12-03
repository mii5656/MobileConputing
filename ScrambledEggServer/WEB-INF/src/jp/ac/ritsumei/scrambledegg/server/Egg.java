package jp.ac.ritsumei.scrambledegg.server;


public class Egg {
 
	private int eggID;
	private int teamID;
	private double latitude;
	private double longitude;
	
	
	/**
	 * コンストラクタ 
	 */
	public Egg( int eggID, int teamID, double latitude,double longitude) {
		this.eggID = eggID;
		this.teamID = teamID;
		this.latitude = latitude;
		this.longitude = longitude;
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
}
