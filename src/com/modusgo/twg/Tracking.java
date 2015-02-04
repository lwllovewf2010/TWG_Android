package com.modusgo.twg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class Tracking{
	
	public long id;
	public String timestamp;
	public double latitude;
	public double longitude;
	public double altitude;
	public float heading;
	public float horizontalAccuracy;
	public float verticalAccuracy;
	public int satelites;
	public boolean fixStatus;
	public float speed;
	public String event = "";
	public String rawData = "";
    public boolean blocked;
    
	public Tracking(long timeMillis, double latitude,
			double longitude, double altitude, float heading,
			float horizontalAccuracy, float verticalAccuracy, int satelites,
			boolean fix_status, float speed, String event, String rawData) {
		super();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMillis);
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.timestamp = sdf.format(c.getTime());
		
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.heading = heading;
		this.horizontalAccuracy = horizontalAccuracy;
		this.verticalAccuracy = verticalAccuracy;
		this.satelites = satelites;
		this.fixStatus = fix_status;
		this.speed = speed;
		this.event = event;
		this.rawData = rawData;
		this.blocked = false;
	}
	
	public Tracking(String event){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_ZULU,Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.timestamp = sdf.format(c.getTime());
		this.event = event;
	}
}
