package com.modusgo.twg.utils;

public class Maintenance {
	public long id;
	public String created_at;
	public String description;
	public String importance;
	public String mileage;
	public float price;
	public int countdown;
	
	public Maintenance(long id, String created_at, String description,
			String importance, String mileage, float price, int countdown) {
		super();
		this.id = id;
		this.created_at = created_at;
		this.description = description;
		this.importance = importance;
		this.mileage = mileage;
		this.price = price;
		this.countdown = countdown;
	}

	public Maintenance(long id, String created_at, String description,
			String importance, String mileage, float price) {
		super();
		this.id = id;
		this.created_at = created_at;
		this.description = description;
		this.importance = importance;
		this.mileage = mileage;
		this.price = price;
		this.countdown = Integer.parseInt(mileage);
	}
}


