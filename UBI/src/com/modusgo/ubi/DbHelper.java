package com.modusgo.ubi;

import java.util.ArrayList;

public class DbHelper {
	
	public DbHelper() {
		// TODO Auto-generated constructor stub
	}
	
	public static ArrayList<Driver> getDrivers(){
		ArrayList<Driver> data = new ArrayList<Driver>();
		data.add(new Driver(1, "Melissa Hasalonglastname", R.drawable.person_test, "2012 Ford Edge", "000-000-0000", "M.HASALONGLASTNAME@gmail.com", "07/05/2014 05:00 PM PST", 10, 3,"C"));
		data.add(new Driver(2, "Diana Johnson", R.drawable.person_test2, "2011 Ford Focus", "111-222-3333", "D.JOHNSON@gmail.com", "07/05/2014 05:00 PM PST", 5, 1, "B"));
		data.add(new Driver(3, "Kate Summerton", R.drawable.person_test3, "1967 Ford Mustang", "123-4546-7890", "K.SUMMERTON@gmail.com", "07/05/2014 05:00 PM PST", 12, 0, "A+"));
		return data;
	}

}
