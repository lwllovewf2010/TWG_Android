package com.modusgo.ubi;

import java.util.ArrayList;

public class DbHelper {
	
	public DbHelper() {
		// TODO Auto-generated constructor stub
	}
	
	public static ArrayList<Driver> getDrivers(){
		ArrayList<Driver> data = new ArrayList<Driver>();
		data.add(new Driver("Melissa Hasalonglastname", R.drawable.person_test, "2012 Ford Edge","07/05/2014 05:00 PM PST", true, true, 10, 3,"C"));
		data.add(new Driver("Diana Johnson", R.drawable.person_test2, "2011 Ford Focus","07/05/2014 05:00 PM PST", true, false, 5, 1, "B"));
		data.add(new Driver("Kate Summerton", R.drawable.person_test3, "1967 Ford Mustang","07/05/2014 05:00 PM PST", false, true, 12, 0, "A+"));
		return data;
	}

}
