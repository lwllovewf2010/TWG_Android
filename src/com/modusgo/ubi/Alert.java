package com.modusgo.ubi;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

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
    public String geofenceString = "";
    public ArrayList<LatLng> geofence;
	
	public Alert(long id) {
		super();
		this.id = id;
		geofence = new ArrayList<LatLng>();
	}
	
	public void setGeofence(String geofenceJSONStr){
		geofenceString = geofenceJSONStr;
		
		System.out.println(geofenceJSONStr);
		
		JSONArray geofenceJSON;
		try {
			geofenceJSON = new JSONArray(geofenceJSONStr);
		
			geofence.clear();
			for (int j = 0; j < geofenceJSON.length(); j++) {
				try {
					JSONArray point = geofenceJSON.getJSONArray(j);
					geofence.add(new LatLng(point.getDouble(0), point.getDouble(1)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
