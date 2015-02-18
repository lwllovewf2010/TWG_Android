package com.modusgo.ubi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.R;

public class TimeAgoUtils {
	
	public static String getTimeAgo(String lastTripDate, Context ctx){
		
		Date lastTrip = convertToDate(lastTripDate);

		long time = lastTrip.getTime();

		Date curDate = currentDate();
		long now = curDate.getTime();
		if (time > now || time <= 0) {
			return null;
		}

		int dim = getTimeDistanceInMinutes(time);

		String timeAgo = null;

		if (dim == 0) {
			timeAgo = ctx.getResources().getString(R.string.just_now);
		} else if (dim == 1) {
			return "1 " + ctx.getResources().getString(R.string.date_util_unit_minute);
		} else if (dim >= 2 && dim <= 44) {
			timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
		} else if (dim >= 45 && dim <= 89) {
			timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_hour);
		} else if (dim >= 90 && dim <= 1439) {
			timeAgo = (Math.round(dim / 60)) + " " + ctx.getResources().getString(R.string.date_util_unit_hours);
		} else if (dim >= 1440 && dim <= 2519) {
			timeAgo = ctx.getResources().getString(R.string.yesterday);
		} else if (dim >= 2520 && dim <= 10079) {
			timeAgo = (Math.round(dim / 1440)) + " " + ctx.getResources().getString(R.string.date_util_unit_days);
		} else if (dim >= 10080 && dim <= 10081) {
			timeAgo = ctx.getResources().getString(R.string.last_week);
		} else if (dim >= 10081 && dim <= 43199) {
			timeAgo = (Math.round(dim / 10080)) + " " + ctx.getResources().getString(R.string.week_ago);
		}else if (dim >= 43200 && dim <= 86399) {
			timeAgo = ctx.getResources().getString(R.string.last_month);
		} else if (dim >= 86400 && dim <= 525599) {
			timeAgo = (Math.round(dim / 43200)) + " " + ctx.getResources().getString(R.string.date_util_unit_months);
		} else if (dim >= 525600 && dim <= 1051199) {
			timeAgo = ctx.getResources().getString(R.string.last_year);
		} else {
			timeAgo = (Math.round(dim / 525600)) + " " + ctx.getResources().getString(R.string.date_util_unit_years);
		}

		return timeAgo;
	}

	private static int getTimeDistanceInMinutes(long time) {
		long timeDistance = currentDate().getTime() - time;
		return Math.round((Math.abs(timeDistance) / 1000) / 60);
	}

	public static Date currentDate() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

	public static Date convertToDate(String dateString){
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return convertedDate;
	}

}
