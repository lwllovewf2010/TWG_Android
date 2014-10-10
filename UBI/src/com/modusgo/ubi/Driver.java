package com.modusgo.ubi;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.modusgo.ubi.utils.Utils;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public long id;
	public String name = "";
	public String photo = "";
	public String markerIcon = "";

	public String carVIN = "";
	public String carMake = "";
	public String carModel = "";
	public String carYear = "";
	public int carFuelLevel;
	public boolean carCheckup;

	public int diags;
	public int alerts;
	
	public double latitude;
	public double longitude;
	public String address = "";
	public String lastTripDate = "";
	public long lastTripId;
	
	public int score;
	public String grade = "";
	public int totalTripsCount;
	public int totalDrivingTime;
	public double totalDistance;
	public int totalBraking;
	public int totalAcceleration;
	public int totalSpeeding;
	public double totalSpeedingDistance;
	
	public String profileDate = "";
	public int harshEvents;
	
	public ArrayList<ListItem> tripsMap = new ArrayList<ListItem>();
	
	public Driver(){
	}
	
	public String getFirstName(){
		return name.split(" ")[0];
	}
	
	public String getLastName(){
		String lastName ="";
		try {
			lastName = name.split(" ")[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastName;
	}
	
	public String getCarFullName(){
		return carYear + " " + carMake + " " + carModel;
	}
	
	public static Driver fromJSON(JSONObject vehicleJSON) throws JSONException{
		Driver d = new Driver();
		d.id = vehicleJSON.getLong("id");
		
		if(!vehicleJSON.isNull("driver")){
			JSONObject driverJSON = vehicleJSON.getJSONObject("driver");
			d.name = driverJSON.optString("name");
			d.photo = driverJSON.optString("photo");
			d.markerIcon = driverJSON.optString("icon");
		}
		
		if(!vehicleJSON.isNull("car")){
			JSONObject carJSON = vehicleJSON.getJSONObject("car");
			d.carVIN = carJSON.optString("vin");
			d.carMake = carJSON.optString("make");
			d.carModel = carJSON.optString("model");
			d.carYear = carJSON.optString("year");
			d.carFuelLevel = carJSON.optInt("fuel_level", -1);
			d.carCheckup = carJSON.optBoolean("checkup");
		}
		
		if(!vehicleJSON.isNull("location")){
			JSONObject locationJSON = vehicleJSON.getJSONObject("location");
			d.latitude = locationJSON.optDouble("latitude");
			d.longitude = locationJSON.optDouble("longitude");
			d.address = locationJSON.optString("address");
			d.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString("last_trip_time","Undefined"));
			d.lastTripId = locationJSON.optLong("last_trip_id");
		}
		
		if(!vehicleJSON.isNull("stats")){
			JSONObject statsJSON = vehicleJSON.getJSONObject("stats");
			d.score = statsJSON.optInt("score");
			d.grade = statsJSON.optString("grade");
			d.totalTripsCount = statsJSON.optInt("trips");
			d.totalDrivingTime = statsJSON.optInt("time");
			d.totalDistance = statsJSON.optDouble("distance");
			d.totalBraking = statsJSON.optInt("braking");
			d.totalAcceleration = statsJSON.optInt("acceleration");
			d.totalSpeeding = statsJSON.optInt("speeding");
			d.totalSpeedingDistance = statsJSON.optDouble("speeding_distance");
			d.alerts = statsJSON.optInt("new_alerts");
		}
		
		return d;
	}

}
