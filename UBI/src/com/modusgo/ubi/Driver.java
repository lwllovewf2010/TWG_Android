package com.modusgo.ubi;

import java.io.Serializable;
import java.util.ArrayList;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public long id;
	public String name;
	public String markerIcon;
	public String photo = "";

	public String carVIN;
	public String carMake;
	public String carModel;
	public String carYear;
	public int carFuelLevel;
	public boolean carCheckup;

	public int diags;
	public int alerts;
	
	public double latitude;
	public double longitude;
	public String address;
	public String lastTripDate;
	public long lastTripId;
	
	public int score;
	public String grade;
	public int totalTripsCount;
	public int totalDrivingTime;
	public double totalDistance;
	public int totalBraking;
	public int totalAcceleration;
	public int totalSpeeding;
	public double totalSpeedingDistance;
	
	public String profileDate;
	public int harshEvents;
	
	public ArrayList<ListItem> tripsMap = new ArrayList<ListItem>();
	
	public Driver(){
	}
	
	public String getFirstName(){
		return name.split(" ")[0];
	}
	
	public String getLastName(){
		String lastName ="";
		try {
			lastName = name.split(" ")[1];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastName;
	}
	
	public String getCarFullName(){
		return carYear + " " + carMake + " " + carModel;
	}

}
