package com.modusgo.ubi;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
	ArrayList<ArrayList<LatLng>> speedingRoute;
	ArrayList<Event> events;
	public String grade;
	public int fuelLevel;
	public String fuelUnit="";
	
	public enum EventType {START, STOP, HARSH_BRAKING, HARSH_ACCELERATION, SPEEDING, PHONE_USAGE, APP_USAGE};
	
	private static SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
	private static SimpleDateFormat sdfTo = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	
	
	public Trip(long id, int eventsCount, String startDate, String endDate, double distance) {
		super();
		this.id = id;
		this.eventsCount = eventsCount;
		this.startDate = startDate;
		this.endDate = endDate;
		route = new ArrayList<LatLng>();
		points = new ArrayList<Point>();
		speedingRoute = new ArrayList<ArrayList<LatLng>>();
		events = new ArrayList<Event>();
		
		this.distance = distance;
	}
	
	public Trip(long id, int eventsCount, String startDate, String endDate, double distance, String grade) {
		this(id, eventsCount, startDate, endDate, distance);
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
		ArrayList<EventType> events;
		
		public Point(LatLng location, ArrayList<EventType> events) {
			super();
			this.location = location;
			this.events = events;
		}
		
		public double getLatitude(){
			return location.latitude;
		}
		
		public double getLongitude(){
			return location.longitude;
		}
		
		public String getEventsString(){
			String eventsStr = "";
			for (EventType ev : events) {
				eventsStr+=ev.toString()+" ";
			}
			
			return eventsStr.substring(0, eventsStr.length()-1);
		}
	}
	
	static public class Event {
		
		public EventType type;
		public String title;
		public String address;
		
		public Event(EventType type, String title, String address) {
			super();
			this.type = type;
			this.title = title;
			this.address = address;
		}
	}
	
}
