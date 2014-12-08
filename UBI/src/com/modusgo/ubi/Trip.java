package com.modusgo.ubi;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

public class Trip extends ListItem implements Serializable{
	
	private static final long serialVersionUID = 2355015935997524870L;
	public long id;
	public int eventsCount;
	public String startDate;
	public String endDate;
	public double distance;
	public double averageSpeed;
	public double maxSpeed;
	ArrayList<LatLng> route;
	ArrayList<Point> points;
	ArrayList<ArrayList<LatLng>> speedingRoutes;
	public String grade;
	public float fuel = 0;
	public String fuelUnit="";
	public float fuelCost = 0;
	public String fuelStatus = "";
	public boolean viewed = false;
	public String viewedAt = "";
	public String updatedAt = "";
	public boolean hidden = false;
	
	public enum EventType {START, STOP, HARSH_BRAKING, HARSH_ACCELERATION, SPEEDING, CALL_USAGE, PHONE_USAGE, UNKNOWN};
	
	private static SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
	private static SimpleDateFormat sdfTo = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	private static TimeZone tzFrom;
	private static TimeZone tzTo;
	
	public Trip(SharedPreferences prefs, long id, int eventsCount, String startDate, String endDate, double distance) {
		super();
		this.id = id;
		this.eventsCount = eventsCount;
		this.startDate = startDate;
		this.endDate = endDate;
		route = new ArrayList<LatLng>();
		points = new ArrayList<Point>();
		speedingRoutes = new ArrayList<ArrayList<LatLng>>();
		
		this.distance = distance;
		
		updateTimezones(prefs);
	}
	
	public static void updateTimezones(SharedPreferences prefs){
		tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
		sdfFrom.setTimeZone(tzFrom);
		tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfTo.setTimeZone(tzTo);
	}
	
	public Trip(SharedPreferences prefs, long id, int eventsCount, String startDate, String endDate, double distance, String grade) {
		this(prefs, id, eventsCount, startDate, endDate, distance);
		this.grade = grade;
	}
	
	public String getStartDateString() {
		return sdfTo.format(getStartDate());
	}
	
	public Date getStartDate() {
		Date date = null;
		try {
			date = sdfFrom.parse(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public String getEndDateString() {
		return sdfTo.format(getEndDate());
	}
	
	public Date getEndDate() {
		Date date = null;
		try {
			date = sdfFrom.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	static public class Point {
		
		LatLng location;
		EventType event;
		String title;
		String address;
		
		public Point(LatLng location, EventType event, String title, String address) {
			super();
			this.location = location;
			this.event = event;
			this.title = title;
			this.address = address;
		}
		
		public double getLatitude(){
			return location.latitude;
		}
		
		public double getLongitude(){
			return location.longitude;
		}
		
		public String getEvent(){
			return event.toString();
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getAddress() {
			return address;
		}
		
		public void fetchAddress(Context context){
			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(context, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
				if(addresses.size()>0)
					address = addresses.get(0).getAddressLine(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
