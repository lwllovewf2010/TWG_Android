package com.modusgo.ubi.db;

import android.provider.BaseColumns;

public class VehicleContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public VehicleContract() {}

    /* Inner class that defines the table contents */
    public static abstract class VehicleEntry implements BaseColumns {
        public static final String TABLE_NAME = "vehicles";
        public static final String COLUMN_NAME_DRIVER_NAME = "driver_name";
        public static final String COLUMN_NAME_DRIVER_MARKER_ICON = "marker_icon";
        public static final String COLUMN_NAME_DRIVER_PHOTO = "driver_photo";
        public static final String COLUMN_NAME_CAR_VIN = "vin";
        public static final String COLUMN_NAME_CAR_MAKE = "make";
        public static final String COLUMN_NAME_CAR_MODEL = "model";
        public static final String COLUMN_NAME_CAR_YEAR = "year";
        public static final String COLUMN_NAME_CAR_FUEL = "fuel_level";
        public static final String COLUMN_NAME_CAR_FUEL_UNIT = "fuel_unit";
        public static final String COLUMN_NAME_CAR_FUEL_STATUS = "fuel_status";
        public static final String COLUMN_NAME_CAR_DTC_COUNT = "dtc_count";
        public static final String COLUMN_NAME_CAR_LAST_CHECKUP = "last_checkup";
        public static final String COLUMN_NAME_CAR_CHECKUP_STATUS = "checkup_status";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_LAST_TRIP_DATE = "last_trip_date";
        public static final String COLUMN_NAME_LAST_TRIP_ID = "last_trip_id";
        public static final String COLUMN_NAME_IN_TRIP = "in_trip";

        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_GRADE = "grade";
        public static final String COLUMN_NAME_TOTAL_TRIPS_COUNT = "total_trips";
        public static final String COLUMN_NAME_TOTAL_DRIVING_TIME = "total_driving_time";
        public static final String COLUMN_NAME_TOTAL_DISTANCE = "total_distance";
        public static final String COLUMN_NAME_TOTAL_BREAKING = "total_breaking";
        public static final String COLUMN_NAME_TOTAL_ACCELERATION = "total_acceleration";
        public static final String COLUMN_NAME_TOTAL_SPEEDING = "total_speeding";
        public static final String COLUMN_NAME_TOTAL_SPEEDING_DISTANCE = "total_speeding_distance";
        public static final String COLUMN_NAME_ALERTS = "new_alerts";
        public static final String COLUMN_NAME_ODOMETER = "odometer";
        public static final String COLUMN_NAME_LIMITS_BLOCKED = "limits_blocked";
        public static final String COLUMN_NAME_LIMITS_BLOCKED_BY = "limits_blocked_by";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }
}