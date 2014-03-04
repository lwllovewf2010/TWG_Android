package com.modusgo.ubi;

import java.io.Serializable;

public class Driver implements Serializable{
	
	private static final long serialVersionUID = 1315237349232671000L;
	public String name;
	public String score;
	
	public Driver(String name, String score){
		this.name = name;
		this.score = score;
	}

}
