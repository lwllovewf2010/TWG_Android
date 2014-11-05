package com.modusgo.ubi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;

import com.modusgo.ubi.Constants;

public class Utils {
	
	public static final String md5(final String s) {
		if(s==null || s.equals(""))
			return "";
		
	    final String MD5 = "MD5";
	    final String salt = "android";
	    String forHash = s+salt;
	    
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance(MD5);
	        digest.update(forHash.getBytes());
	        byte messageDigest[] = digest.digest();

	        // Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        for (byte aMessageDigest : messageDigest) {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();

	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public static String getUUID(Context context){
	    final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    String deviceId = deviceUuid.toString();
	    return md5(deviceId);
	}
	
	public static JSONObject getJSONObjectFromAssets(Context context, String filename){
		String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        try {
			return new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject getJSONObjectFromHttpResponse(HttpResponse response){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			
			StringBuilder builder = new StringBuilder();
			String aux = "";
			while ((aux = reader.readLine()) != null) {
			    builder.append(aux);
			}
			String json = builder.toString();
			JSONTokener tokener = new JSONTokener(json);
			return new JSONObject(tokener);
		}
		catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String fixTimezoneZ(String datetime){
		return datetime.replace("Z", "-00:00");
	}
	
	public static String fixTimeZoneColon(String oldDate){
		int length = oldDate.length();
		return oldDate.substring(0, length - 2) + ':' + oldDate.substring(length - 2);
	}
	
	public static String convertTime(String date, SimpleDateFormat sdfTo){
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		try {
			return sdfTo.format(sdfFrom.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
	}
	
	public static float metersToMiles(float meters){
		return meters*0.00062137f;
	}
	
	public static int durationInMinutes(Date startDate, Date endDate)
    {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);
		
        int years 	= endCalendar.get(Calendar.YEAR) 		- startCalendar.get(Calendar.YEAR);
        int days 	= endCalendar.get(Calendar.DAY_OF_YEAR) - startCalendar.get(Calendar.DAY_OF_YEAR);
        int hours 	= endCalendar.get(Calendar.HOUR_OF_DAY) - startCalendar.get(Calendar.HOUR_OF_DAY);
        int mins 	= endCalendar.get(Calendar.MINUTE) 		- startCalendar.get(Calendar.MINUTE);

        if (mins < 0) {
            hours = hours - 1;
            mins  = mins + 60;
        }

        if (hours < 0) {
            days  = days - 1;
            hours = hours + 24;
        }

        // Leap year corrections
        int daysInYear = 365;
        Calendar leapYear = Calendar.getInstance();
        leapYear.set( startCalendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
        if (leapYear.get(Calendar.DAY_OF_YEAR) == 366) {
            leapYear.set( startCalendar.get(Calendar.YEAR), 1, 29, 23, 59, 59);
            if (startCalendar.before(leapYear))
                daysInYear = 366;
        }

        leapYear.set( endCalendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
        if (leapYear.get(Calendar.DAY_OF_YEAR) == 366) {
            leapYear.set( endCalendar.get(Calendar.YEAR), 1, 29, 23, 59, 59);
            if (endCalendar.after(leapYear)) {
                daysInYear = 366;
                if (years > 0)
                    days = days - 1;
            }
        }

        if (days < 0) {
            years--;
            days = days + daysInYear;
        }

        return mins+hours*60+days*24*60+years*daysInYear*24*60;   
    }
	
	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
	    int childCount = viewGroup.getChildCount();
	    for (int i = 0; i < childCount; i++) {
	      View view = viewGroup.getChildAt(i);
	      view.setEnabled(enabled);
	      if (view instanceof ViewGroup) {
	        enableDisableViewGroup((ViewGroup) view, enabled);
	      }
	    }
	  }
}
