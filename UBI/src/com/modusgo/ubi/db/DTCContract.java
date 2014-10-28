package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class DTCContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public DTCContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class DTCEntry implements BaseColumns {
		public static final String TABLE_NAME = "dtc";
		public static final String COLUMN_NAME_DRIVER_ID = "driver_id";
		public static final String COLUMN_NAME_CODE = "code";
		public static final String COLUMN_NAME_CONDITIONS = "conditions";
		public static final String COLUMN_NAME_CREATED_AT = "created_at";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_DETAILS = "details";
		public static final String COLUMN_NAME_FULL_DESCRIPTION = "full_description";
		public static final String COLUMN_NAME_IMPORTANCE = "importance";
		public static final String COLUMN_NAME_LABOR_COST = "labor_cost";
		public static final String COLUMN_NAME_LABOR_HOURS = "labor_hours";
		public static final String COLUMN_NAME_PARTS = "parts";
		public static final String COLUMN_NAME_PARTS_COST = "parts_cost";
		public static final String COLUMN_NAME_TOTAL_COST = "total_cost";
	}
}