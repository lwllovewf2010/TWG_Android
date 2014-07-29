package com.modusgo.ubi;

import java.io.Serializable;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public String name;
	public String vehicle;
	public String lastTripDate;
	public String score;
	
	public Driver(String name, String score){
		this.name = name;
		this.score = score;
	}
	
	public Driver(String name, String vehicle, String lastTripDate){
		this.name = name;
		this.vehicle = vehicle;
		this.lastTripDate = lastTripDate;
	}

}
