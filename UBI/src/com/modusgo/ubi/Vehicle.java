package com.modusgo.ubi;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import com.modusgo.ubi.utils.Utils;

public class Vehicle implements Serializable{
	
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
	public String carFuelUnit="";
	public String carFuelStatus="";
	public int carDTCCount;
	public String carLastCheckup="";
	public String carCheckupStatus="";

	public int alerts;
	
	public double latitude;
	public double longitude;
	public String address = "";
	public String lastTripDate = "";
	public long lastTripId;
	public boolean inTrip;
	
	public int score;
	public String grade = "";
	public int totalTripsCount;
	public int totalDrivingTime;
	public double totalDistance;
	public int totalBraking;
	public int totalAcceleration;
	public int totalSpeeding;
	public double totalSpeedingDistance;
	public int odometer;
	
	public String profileDate = "";
	public int harshEvents;
	public boolean limitsBlocked;
	public String limitsBlockedBy = "";
	public String updatedAt = "";
	
	public Vehicle(){
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
	
	public static Vehicle fromJSON(Context context, JSONObject vehicleJSON) throws JSONException{
		Vehicle v = new Vehicle();
		v.id = vehicleJSON.getLong("id");
		v.alerts = vehicleJSON.optInt("count_new_alerts");
		v.limitsBlocked = vehicleJSON.optBoolean("limits_blocked");
		v.limitsBlockedBy = vehicleJSON.optString("limits_blocked_by");
		
		if(!vehicleJSON.isNull("driver")){
			JSONObject driverJSON = vehicleJSON.getJSONObject("driver");
			v.name = driverJSON.optString("name");
			v.photo = driverJSON.optString("photo");
			v.markerIcon = driverJSON.optString("icon");
		}
		
		if(!vehicleJSON.isNull("car")){
			JSONObject carJSON = vehicleJSON.getJSONObject("car");
			v.carVIN = carJSON.optString("vin");
			v.carMake = carJSON.optString("make");
			v.carModel = carJSON.optString("model");
			v.carYear = carJSON.optString("year");
			v.carFuelLevel = carJSON.optInt("fuel_level", -1);
			v.carFuelUnit = carJSON.optString("fuel_unit");
			v.carFuelStatus = carJSON.optString("fuel_status");
			v.carDTCCount = carJSON.optInt("dtc_count");
			v.carLastCheckup = carJSON.optString("last_checkup");
			v.carCheckupStatus = carJSON.optString("checkup_status");
			v.odometer = carJSON.optInt("odometer");
		}
		
		if(!vehicleJSON.isNull("location")){
			JSONObject locationJSON = vehicleJSON.getJSONObject("location");
			v.latitude = locationJSON.optDouble("latitude", 0);
			v.longitude = locationJSON.optDouble("longitude", 0);
			v.address = locationJSON.optString("address");
			if(TextUtils.isEmpty(v.address)){
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(context, Locale.getDefault());
				try {
					addresses = geocoder.getFromLocation(v.latitude, v.longitude, 1);
					if(addresses.size()>0)
						v.address = addresses.get(0).getAddressLine(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			v.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString("last_trip_time","Undefined")).equals("null") ? "" : Utils.fixTimezoneZ(locationJSON.optString("last_trip_time","Undefined"));
			v.lastTripId = locationJSON.optLong("last_trip_id");
			v.inTrip = !locationJSON.isNull("in_trip");
		}
		
		if(!vehicleJSON.isNull("stats")){
			JSONObject statsJSON = vehicleJSON.getJSONObject("stats");
			v.score = statsJSON.optInt("score");
			v.grade = statsJSON.optString("grade");
			v.totalTripsCount = statsJSON.optInt("trips");
			v.totalDrivingTime = statsJSON.optInt("time");
			v.totalDistance = statsJSON.optDouble("distance");
			v.totalBraking = statsJSON.optInt("braking");
			v.totalAcceleration = statsJSON.optInt("acceleration");
			v.totalSpeeding = statsJSON.optInt("speeding");
			v.totalSpeedingDistance = statsJSON.optDouble("speeding_distance");
		}
		
		return v;
	}

}
