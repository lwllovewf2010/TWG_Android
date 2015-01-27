package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class WarrantyInfoContract {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public WarrantyInfoContract() {
	}

	/* Inner class that defines the table contents */
	public static abstract class WarrantyInfoEntry implements BaseColumns {
		public static final String TABLE_NAME = "warranty_information";
		public static final String COLUMN_NAME_VEHICLE_ID = "vehicle_id";
		public static final String COLUMN_NAME_CREATED_AT = "created_at";
		public static final String COLUMN_NAME_DESCRIPTION = "description";
		public static final String COLUMN_NAME_MILEAGE = "mileage";
	}
}