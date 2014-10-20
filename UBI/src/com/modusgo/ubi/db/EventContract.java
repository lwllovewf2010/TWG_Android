package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class EventContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public EventContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class EventEntry implements BaseColumns {
		public static final String TABLE_NAME = "events";
		public static final String COLUMN_NAME_TRIP_ID = "trip_id";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_ADDRESS = "address";
	}
}