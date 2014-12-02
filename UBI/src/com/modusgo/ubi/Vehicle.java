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
	public String carLastCheckup;
	public String carCheckupStatus;

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
	public int odometer;
	
	public String profileDate = "";
	public int harshEvents;
	
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
		Vehicle d = new Vehicle();
		d.id = vehicleJSON.getLong("id");
		d.alerts = vehicleJSON.optInt("count_new_alerts");
		
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
			d.carFuelLevel = carJSON.optInt("fuel_left", -1);
			d.carFuelUnit = carJSON.optString("fuel_unit");
			d.carFuelStatus = carJSON.optString("fuel_status");
			d.carDTCCount = carJSON.optInt("dtc_count");
			d.carLastCheckup = carJSON.optString("last_checkup");
			d.carCheckupStatus = carJSON.optString("checkup_status");
			d.odometer = carJSON.optInt("odometer");
		}
		
		if(!vehicleJSON.isNull("location")){
			JSONObject locationJSON = vehicleJSON.getJSONObject("location");
			d.latitude = locationJSON.optDouble("latitude");
			d.longitude = locationJSON.optDouble("longitude");
			d.address = locationJSON.optString("address");
			if(TextUtils.isEmpty(d.address)){
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(context, Locale.getDefault());
				try {
					addresses = geocoder.getFromLocation(d.latitude, d.longitude, 1);
					if(addresses.size()>0)
						d.address = addresses.get(0).getAddressLine(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
		}
		
		return d;
	}

}
