package com.modusgo.ubi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public long id;
	public String name;
	public int imageId;
	public String imageUrl;
	public String vehicle;
	public String VIN;
	public String lastTripDate;
	public String profileDate;
	public String score;
	public String phone;
	public String email;
	public int diags;
	public int alerts;
	public int tripsCount;
	public int harshEvents;
	public double latitude;
	public double longitude;
	public int fuelLeft;
	public String address;
	
	public LinkedHashMap<String, ArrayList<Trip>> tripsMap = new LinkedHashMap<>();
	
	public Driver(String name, String score){
		this.name = name;
		this.score = score;
	}
	
	public Driver(long id, String name, int imageId, String vehicle, String phone, String email, String lastTripDate, int tripsCount, int harshEvents, String score){
		this.id = id;
		this.name = name;
		this.imageId = imageId;
		this.vehicle = vehicle;
		this.phone = phone;
		this.email = email;
		this.lastTripDate = lastTripDate;
		this.tripsCount = tripsCount;
		this.harshEvents = harshEvents;
		this.score = score;
		this.address = "";
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
