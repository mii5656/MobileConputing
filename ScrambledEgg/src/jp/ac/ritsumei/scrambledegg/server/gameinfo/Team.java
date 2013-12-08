package jp.ac.ritsumei.scrambledegg.server.gameinfo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Team {


	private String teamName;
	private int teamID;
	private double baseLatitude;
	private double baseLongitude;
	private List<Player> playersList;
	private List<Egg> eggsList;

	
	public Team(String teamName, int teamID){
		this.teamName = teamName;
		this.teamID = teamID;
		playersList = new ArrayList<Player>();
		eggsList = new ArrayList<Egg>();
	}

	public void addPlayerList(Player p){
		p.setTeamID(teamID);
		playersList.add(p);
	}

	public void addEggList(Egg e){
		e.setTeamID(teamID);
		eggsList.add(e);
	}


	public JSONObject getJSONObject(){
		JSONObject result = new JSONObject();
		try {
			result.put("teamName", teamName);
			result.put("teamID", teamID);
			result.put("latitude", baseLatitude);
			result.put("longitude", baseLongitude);
			result.put("teamID", teamID);
			
			
			//ARRAY
			JSONArray players = new JSONArray();
			for(Player p : playersList){
				players.put(p.getJSONObject());
			}
			result.put("playersList", players);
			
			JSONArray eggs = new JSONArray();
			for(Egg e : eggsList){
				eggs.put(e.getJSONObject());
			}
			result.put("eggsList", eggs);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	//以下getter setter
	public double getBaseLatitude() {
		return baseLatitude;
	}
	public void setBaseLatitude(double baseLatitude) {
		this.baseLatitude = baseLatitude;
	}
	public double getBaseLongitude() {
		return baseLongitude;
	}
	public void setBaseLongitude(double baseLongitude) {
		this.baseLongitude = baseLongitude;
	}
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
