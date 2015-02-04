package com.modusgo.twg.db;

import android.provider.BaseColumns;

public class ServicePerformedContract
{
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ServicePerformedContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ServicePerformedEntry implements BaseColumns {
		public static final String TABLE_NAME = "service_performed";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_LOCATION = "location";
		public static final String COLUMN_NAME_MILAGE = "milage";
	}
}
