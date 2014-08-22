package com.modusgo.ubi;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Trip implements Serializable{
	
	private static final long serialVersionUID = 2355015935997524870L;
	long id;
	int eventsCount;
	private Date startDate;
	private Date endDate;
	double distance;
	
	private static SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
	private static SimpleDateFormat sdfTo = new SimpleDateFormat("hh:mm a", Locale.getDefault());
	
	
	public Trip(long id, int eventsCount, String startDate, String endDate, double distance) {
		super();
		this.id = id;
		this.eventsCount = eventsCount;
		
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
	
}
