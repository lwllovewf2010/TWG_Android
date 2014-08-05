package com.modusgo.ubi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Trip {
	
	private static final float METERS_TO_MILES = 0.00062137f; 
	
	int eventsCount;
	long startDate;
	long endDate;
	float distance;
	private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	
	public Trip(int eventsCount, long startDate, long endDate, float distance) {
		super();
		this.eventsCount = eventsCount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.distance = distance;
	}
	
	public String getStartDateString() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(startDate);
		return sdf.format(c.getTime());
	}
	
	public String getEndDateString() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(endDate);
		return sdf.format(c.getTime());
	}
	
	public float getDistanceMiles() {
		return distance*METERS_TO_MILES;
	}
	
}
