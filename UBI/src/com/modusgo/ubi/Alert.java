package com.modusgo.ubi;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

public class Alert{
	
	public long id;
	public long vehicleId;
	public long tripId;
	public String type;
	public String timestamp;
	public String title;
	public String description;
	public LatLng location;
	public String seenAt;
	public String address = "";
    public ArrayList<LatLng> geofence;
	
	public Alert(long id) {
		super();
		this.id = id;
	}
}
