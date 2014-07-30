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
	public int tripsCount;
	public int harshEvents;
	
	public Driver(String name, String score){
		this.name = name;
		this.score = score;
	}
	
	public Driver(String name, int imageId, String vehicle, String lastTripDate, boolean diagnosticsOK, boolean alertsOK, int tripsCount, int harshEvents, String score){
		this.name = name;
		this.imageId = imageId;
		this.vehicle = vehicle;
		this.lastTripDate = lastTripDate;
		this.diagnosticsOK = diagnosticsOK;
		this.alertsOK = alertsOK;
		this.tripsCount = tripsCount;
		this.harshEvents = harshEvents;
		this.score = score;
	}
	
	public int getScoreAsNumber(){
		switch (score) {
		case "A+":
			return 9;
		case "A":
			return 8;
		case "A-":
			return 7;
		case "B+":
			return 6;
		case "B":
			return 5;
		case "B-":
			return 4;
		case "C+":
			return 3;
		case "C":
			return 2;
		case "C-":
			return 1;

		default:
			return 0;
		}
	}

}
