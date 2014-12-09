package com.modusgo.ubi;


public class Tracking{
	
	public long id;
	public String timestamp;
	public double latitude;
	public double longitude;
	public double altitude;
	public float heading;
	public float horizontalAccuracy;
	public float verticalAccuracy;
	public int satelites;
	public boolean fix_status;
	public float speed;
	public String event = "";
	public String rawData = "";
    public boolean blocked;
    
	public Tracking(long id, String timestamp, double latitude,
			double longitude, double altitude, float heading,
			float horizontalAccuracy, float verticalAccuracy, int satelites,
			boolean fix_status, float speed, String event, String rawData,
			boolean blocked) {
		super();
		this.id = id;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.heading = heading;
		this.horizontalAccuracy = horizontalAccuracy;
		this.verticalAccuracy = verticalAccuracy;
		this.satelites = satelites;
		this.fix_status = fix_status;
		this.speed = speed;
		this.event = event;
		this.rawData = rawData;
		this.blocked = blocked;
	}
}
