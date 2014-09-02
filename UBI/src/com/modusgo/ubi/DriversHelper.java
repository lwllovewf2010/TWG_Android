package com.modusgo.ubi;

import java.util.ArrayList;

public class DriversHelper {
	
	private static DriversHelper helper = null;
	
	ArrayList<Driver> drivers;
	
	public DriversHelper() {
		drivers = new ArrayList<Driver>();
	}
	
	public static DriversHelper getInstance(){
		if(helper==null){
			helper = new DriversHelper();
		}
		return helper;
	}
	
	public void setDrivers(ArrayList<Driver> drivers){
		this.drivers = drivers;
	}
	
	public ArrayList<Driver> getDrivers(){
		return drivers;
	}
	
	public Driver getDriverById(long id){
		for (Driver driver : drivers) {
			if(driver.id==id)
				return driver;
		}
		return null;
	}
	
	public Driver getDriverByIndex(int i){
		return drivers.get(i);
	}
	
	public void setDriver(int i, Driver d){
		drivers.set(i, d);
	}
	
	public void setDriverById(long id, Driver d){
		for (int j = 0; j < drivers.size(); j++) {
			if(drivers.get(j).id == id){
				drivers.set(j, d);
				break;
			}
		}
	}
}
