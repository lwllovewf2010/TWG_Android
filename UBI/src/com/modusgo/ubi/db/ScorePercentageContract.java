package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class ScorePercentageContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ScorePercentageContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ScorePercentageEntry implements BaseColumns {
		public static final String TABLE_NAME = "score_percentage";
		public static final String COLUMN_NAME_DRIVER_ID = "driver_id";
		public static final String COLUMN_NAME_STAT_NAME = "stat_name";
		public static final String COLUMN_NAME_STAT_VALUE = "stat_value";
	}
}