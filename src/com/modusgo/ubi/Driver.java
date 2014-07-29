package com.modusgo.ubi;

import java.io.Serializable;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public String name;
	public int imageId;
	public String vehicle;
	public String lastTripDate;
	public String score;
	public boolean diagnosticsOK;
	public boolean alertsOK;
	
	public Driver(String name, String score){
		this.name = name;
		this.score = score;
	}
	
	public Driver(String name, int imageId, String vehicle, String lastTripDate, boolean diagnosticsOK, boolean alertsOK){
		this.name = name;
		this.imageId = imageId;
		this.vehicle = vehicle;
		this.lastTripDate = lastTripDate;
		this.diagnosticsOK = diagnosticsOK;
		this.alertsOK = alertsOK;
	}

}
