package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class ScoreInfoContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ScoreInfoContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ScoreInfoEntry implements BaseColumns {
		public static final String TABLE_NAME = "score_info";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_PROFILE_DATE = "profile_date";
		public static final String COLUMN_NAME_START_DATE = "start_date";
		public static final String COLUMN_NAME_PROFILE_DRIVING_MILES = "profile_driving_miles";
		public static final String COLUMN_NAME_ESTIMATED_ANNUAL_DRIVING = "estimated_annual_driving";
	}
}