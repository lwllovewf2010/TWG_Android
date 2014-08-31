package com.modusgo.ubi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.modusgo.ubi.Constants;

import android.content.Context;
import android.telephony.TelephonyManager;

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
	
	public static String convertTime(String date, SimpleDateFormat sdfTo){
		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		try {
			return sdfTo.format(sdfFrom.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
			return date;
		}
	}
	
}
