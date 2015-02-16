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
	
	//fields
	private final static String ID = "id";
	private final static String COUNT_NEW_ALERTS = "count_new_alerts";
	private final static String LIMITS_BLOCKED = "limits_blocked";
	private final static String LIMITS_BLOCKED_BY = "limits_blocked_by";
	private final static String DRIVER = "driver";
	private final static String NAME = "name";
	private final static String PHOTO = "photo";
	private final static String ICON = "icon";
	private final static String CAR = "car";
	private final static String VIN = "vin";
	private final static String MAKE = "make";
	private final static String MODEL = "model";
	private final static String YEAR = "year";
	private final static String FUEL_LEVEL = "fuel_level";
	private final static String FUEL_UNIT = "fuel_unit";
	private final static String FUEL_STATUS = "fuel_status";
	private final static String DTC_COUNT = "dtc_count";
	private final static String LAST_CHECKUP = "last_checkup";
	private final static String UNDEFINED = "undefined";
	private final static String CHECKUP_STATUS = "checkup_status";
	private final static String ODOMETER = "odometer";
	private final static String LOCATION = "location";
	private final static String LATITUDE = "latitude";
	private final static String LONGITUDE = "longitude";
	private final static String ADDRESS = "address";
	private final static String NULL = "null";
	private final static String LAST_TRIP_TIME = "last_trip_time";
	private final static String LAST_TRIP_ID = "last_trip_id";
	private final static String IN_TRIP = "in_trip";
	private final static String STATS = "stats";
	private final static String SCORE = "score";
	private final static String GRADE = "grade";
	private final static String TRIPS = "trips";
	private final static String TIME = "time";
	private final static String DISTANCE = "distance";
	private final static String BRAKING = "braking";
	private final static String ACCELERATION = "acceleration";
	private final static String SPEDING = "speeding";
	private final static String SPEDING_DISTANCE = "speeding_distance";
	private final static String HIDE_ENGINE_ICON = "hide_engine_icon";
	
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
	public boolean hideEngineIcon;
	
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
		v.id = vehicleJSON.getLong(ID);
		v.alerts = vehicleJSON.optInt(COUNT_NEW_ALERTS);
		v.limitsBlocked = vehicleJSON.optBoolean(LIMITS_BLOCKED);
		v.limitsBlockedBy = vehicleJSON.optString(LIMITS_BLOCKED_BY);
		v.hideEngineIcon = vehicleJSON.optBoolean(HIDE_ENGINE_ICON);
		
		if(!vehicleJSON.isNull(DRIVER)){
			JSONObject driverJSON = vehicleJSON.getJSONObject(DRIVER);
			v.name = driverJSON.optString(NAME);
			v.photo = driverJSON.optString(PHOTO);
			v.markerIcon = driverJSON.optString(ICON);
		}
		
		if(!vehicleJSON.isNull(CAR)){
			JSONObject carJSON = vehicleJSON.getJSONObject(CAR);
			v.carVIN = carJSON.optString(VIN);
			v.carMake = carJSON.optString(MAKE);
			v.carModel = carJSON.optString(MODEL);
			v.carYear = carJSON.optString(YEAR);
			v.carFuelLevel = carJSON.optInt(FUEL_LEVEL, -1);
			v.carFuelUnit = carJSON.optString(FUEL_UNIT);
			v.carFuelStatus = carJSON.optString(FUEL_STATUS);
			v.carDTCCount = carJSON.optInt(DTC_COUNT);
			v.carLastCheckup = carJSON.optString(LAST_CHECKUP);
			v.carCheckupStatus = carJSON.optString(CHECKUP_STATUS);
			v.odometer = carJSON.optInt(ODOMETER);
		}
		
		if(!vehicleJSON.isNull(LOCATION)){
			JSONObject locationJSON = vehicleJSON.getJSONObject(LOCATION);
			v.latitude = locationJSON.optDouble(LATITUDE, 0);
			v.longitude = locationJSON.optDouble(LONGITUDE, 0);
			v.address = locationJSON.optString(ADDRESS).equals(NULL) ? "" :  locationJSON.optString(ADDRESS);
			if(TextUtils.isEmpty(v.address)){
				Geocoder geocoder;
				List<Address> addresses;
				geocoder = new Geocoder(context, Locale.US);
				try {
					addresses = geocoder.getFromLocation(v.latitude, v.longitude, 1);
					if(addresses.size()>0)
						v.address = addresses.get(0).getAddressLine(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			v.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString(LAST_TRIP_TIME,UNDEFINED)).equals(NULL) ? "" : Utils.fixTimezoneZ(locationJSON.optString(LAST_TRIP_TIME,UNDEFINED));
			v.lastTripId = locationJSON.optLong(LAST_TRIP_ID);
			v.inTrip = !locationJSON.isNull(IN_TRIP);
		}
		
		if(!vehicleJSON.isNull(STATS)){
			JSONObject statsJSON = vehicleJSON.getJSONObject(STATS);
			v.score = statsJSON.optInt(SCORE);
			v.grade = statsJSON.optString(GRADE);
			v.totalTripsCount = statsJSON.optInt(TRIPS);
			v.totalDrivingTime = statsJSON.optInt(TIME);
			v.totalDistance = statsJSON.optDouble(DISTANCE);
			v.totalBraking = statsJSON.optInt(BRAKING);
			v.totalAcceleration = statsJSON.optInt(ACCELERATION);
			v.totalSpeeding = statsJSON.optInt(SPEDING);
			v.totalSpeedingDistance = statsJSON.optDouble(SPEDING_DISTANCE);
		}
		
		return v;
	}

}
