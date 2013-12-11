package jp.ac.ritsumei.scrambledegg.maps;

import com.google.android.gms.maps.model.Marker;

public class MarkerObject {

	private int kind;
	private int id;
	private Marker marker;

	MarkerObject(int kind, int id, Marker marker) {
		this.id = id;
		this.kind = kind;
		this.marker = marker;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	
}
