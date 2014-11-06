package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class DDEventContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public DDEventContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class DDEventEntry implements BaseColumns {
		public static final String TABLE_NAME = "dd_events";
		public static final String COLUMN_NAME_EVENT = "event";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
	}
}