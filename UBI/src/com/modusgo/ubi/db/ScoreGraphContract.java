package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class ScoreGraphContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public ScoreGraphContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class ScoreGraphEntry implements BaseColumns {
		public static final String TABLE_NAME = "score_graph";
		public static final String COLUMN_NAME_DRIVER_ID = "driver_id";
		public static final String COLUMN_NAME_MONTH = "month";
		public static final String COLUMN_NAME_SCORE = "score";
		public static final String COLUMN_NAME_GRADE = "grade";
	}
}