package com.modusgo.twg.utils;

import java.io.Serializable;
import java.util.Calendar;

public class ServicePerformed implements Serializable
{

	public String description = null;
	public String date_performed = null;
	public String location_performed = null;
	public long milage_when_performed;

	public ServicePerformed(final String description, final String date, final String loc,
			final long milage)
	{
		this.description = description;
		this.date_performed = date;
		this.location_performed = loc;
		this.milage_when_performed = milage;
	}
}
