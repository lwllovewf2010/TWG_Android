package com.modusgo.ubi.utils;

import java.util.Calendar;

public class ServiceEntry
{

	public String description = null;
	public long interval;
	public Calendar date_performed = null;
	public String location_performed = null;
	public long milage_when_performed;

	public ServiceEntry(final String description, final long interval, final Calendar date, final String loc,
			final long milage)
	{
		this.description = description;
		this.interval = interval;
		this.date_performed = date;
		this.location_performed = loc;
		this.milage_when_performed = milage;
	}
}
