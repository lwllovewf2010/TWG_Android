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
	long id;
	int eventsCount;
	private Date startDate;
	private Date endDate;
	double distance;
	double averageSpeed;
	double maxSpeed;
	ArrayList<LatLng> route;
	ArrayList<Point> points;
	ArrayList<ArrayList<LatLng>> speedingRoute;
	ArrayList<Event> events;
	String grade;
	
	enum EventType {START, STOP, HARSH_BRAKING, HARSH_ACCELERATION, SPEEDING, PHONE_USAGE, APP_USAGE};
	
	private static SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
	private static SimpleDateFormat sdfTo = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	
	
	public Trip(long id, int eventsCount, String startDate, String endDate, double distance) {
		super();
		this.id = id;
		this.eventsCount = eventsCount;
		route = new ArrayList<LatLng>();
		points = new ArrayList<Point>();
		speedingRoute = new ArrayList<ArrayList<LatLng>>();
		events = new ArrayList<Event>();
		
		try {
			this.startDate = sdfFrom.parse(startDate);
			this.endDate = sdfFrom.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		this.distance = distance;
	}
	
	public String getStartDateString() {
		return sdfTo.format(startDate);
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public String getEndDateString() {
		return sdfTo.format(endDate);
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	static class Point {
		
		LatLng location;
		ArrayList<EventType> events;
		
		public Point(LatLng location, ArrayList<EventType> events) {
			super();
			this.location = location;
			this.events = events;
		}
	}
	
	static class Event {
		
		EventType type;
		String title;
		String address;
		
		public Event(EventType type, String title, String address) {
			super();
			this.type = type;
			this.title = title;
			this.address = address;
		}
	}
	
}
