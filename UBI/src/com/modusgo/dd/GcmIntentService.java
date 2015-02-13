package com.modusgo.dd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.R;
import com.modusgo.ubi.TripActivity;
import com.modusgo.ubi.Vehicle;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.utils.Device;
import com.modusgo.ubi.utils.Utils;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    
    public static final String TAG = "UBI GCM";        
    private static final String ACTION_DRIVER_UPDATE = "du";
    private static final String ACTION_VEHICLE_UPDATE = "vu";
    private static final String ACTION_NEW_TRIP = "nt";


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
//            	for (String key : extras.keySet()) {
//            	    Object value = extras.get(key);
//            	    Log.d(TAG, String.format("%s %s (%s)", key,  
//            	        value.toString(), value.getClass().getName()));
//            	}
            	
            	Bugsnag.addToTab("User", "Push data", extras.toString());
            	Bugsnag.notify(new RuntimeException("Push received"));            	
            	
            	Calendar calUpdatedAt = Calendar.getInstance();
            	calUpdatedAt.set(Calendar.YEAR, 0);
            	if(extras.containsKey("updated_at")){
            		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
            		try {
            			calUpdatedAt.setTime(sdf.parse(Utils.fixTimezoneZ(extras.getString("updated_at"))));
            		} catch (ParseException ex) {
            			System.out.println("Can't parse push updated_at field :"+Utils.fixTimezoneZ(extras.getString("updated_at")));
            			ex.printStackTrace();
            		}
            	}
            	
            	Intent notificationIntent = null;
            	
            	if(extras.containsKey("act")){
            		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);;
            		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                	
            		switch (extras.getString("act", ACTION_DRIVER_UPDATE)) {
            		case ACTION_DRIVER_UPDATE:
            			Calendar calDeviceUpdatedAt = Calendar.getInstance();
	            		try {
	            			calDeviceUpdatedAt.setTime(sdf.parse(prefs.getString(Device.PREF_DEVICE_UPDATED_AT, "")));
	            		} catch (ParseException ex) {
	            			System.out.println("Can't parse device updated_at field :"+prefs.getString(Device.PREF_DEVICE_UPDATED_AT, ""));
	            			calDeviceUpdatedAt.set(Calendar.YEAR, 0);
	            			ex.printStackTrace();
	            		}
            			
            			String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
            			if(deviceType.equals(Device.DEVICE_TYPE_OBD) && calDeviceUpdatedAt.before(calUpdatedAt)){
            				Editor e  = prefs.edit();
            				if(extras.containsKey("in_trip")){
            					e.putBoolean(Device.PREF_DEVICE_IN_TRIP, !isNull(extras.getString("in_trip")));
            				}
            				else{
            					e.putBoolean(Device.PREF_DEVICE_IN_TRIP, false);
            				}
            				
            				if(extras.containsKey("type")){
            					e.putString(Device.PREF_DEVICE_TYPE, extras.getString("type"));
            				}
            				
            				if(extras.containsKey("events")){
            					try{
            						e.putBoolean(Device.PREF_DEVICE_EVENTS, Boolean.parseBoolean(extras.getString("events")));
            					}
            					catch(Exception ex){
            						ex.printStackTrace();
            						e.putBoolean(Device.PREF_DEVICE_EVENTS, false);
            					}
            				}
            				if(extras.containsKey("latitude")){
            					e.putString(Device.PREF_DEVICE_LATITUDE, extras.getString("latitude"));
            				}
            				if(extras.containsKey("longitude")){
            					e.putString(Device.PREF_DEVICE_LONGITUDE, extras.getString("longitude"));
            				}
            				if(extras.containsKey("location_date")){
            					e.putString(Device.PREF_DEVICE_LOCATION_DATE, extras.getString("location_date"));
            				}
            				if(extras.containsKey("location_date")){
            					e.putString(Device.PREF_DEVICE_LOCATION_DATE, extras.getString("location_date"));
            				}
            				if(extras.containsKey("updated_at")){
            					e.putString(Device.PREF_DEVICE_UPDATED_AT, Utils.fixTimezoneZ(extras.getString("updated_at")));
            				}
            				
                        	e.commit();

            				sendBroadcast(new Intent(Constants.BROADCAST_UPDATE_VEHICLES));
            			}
            			else{
            				Bugsnag.notify(new RuntimeException("Push received, not proccessed, device is not OBD or data is old"));
            			}
            			break;
            		case ACTION_VEHICLE_UPDATE:
            			if(extras.containsKey("id")){
            				DbHelper dbHelper = DbHelper.getInstance(this);
            				Vehicle vehicle;
            				
            				Object id = extras.get("id");
            				if(id instanceof Long)
            					vehicle = dbHelper.getVehicle(extras.getLong("id"));
            				else{
            					vehicle = dbHelper.getVehicle(Long.parseLong(extras.getString("id")));            					
            				}
            				
							Calendar calVehicleUpdatedAt = Calendar.getInstance();
		            		try {
		            			calVehicleUpdatedAt.setTime(sdf.parse(vehicle.updatedAt));
		            		} catch (ParseException ex) {
		            			System.out.println("Can't parse db vehicle updated_at field :"+vehicle.updatedAt);
		            			calVehicleUpdatedAt.set(Calendar.YEAR, 0);
		            			ex.printStackTrace();
		            		}
		            		
							if(calVehicleUpdatedAt.before(calUpdatedAt)){
	            				if(extras.containsKey("location")){
	            					String locationJSONStr = extras.getString("location");
	                        		try {
	                        			JSONObject locationJSON = new JSONObject(locationJSONStr);
	                        			
	                        			if(locationJSON.has("location_date")){
	                        				
	                        			}
	                        			if(locationJSON.has("latitude")){
	                        				vehicle.latitude = locationJSON.optDouble("latitude");
	                        			}
	                        			if(locationJSON.has("longitude")){
	                        				vehicle.longitude = locationJSON.optDouble("longitude");
	                        				System.out.println("longitude: "+locationJSON.optDouble("longitude"));
	                        			}
	                        			if(locationJSON.has("in_trip")){
	                        				vehicle.inTrip = !locationJSON.isNull("in_trip");
	                        				System.out.println("Vehicle in trip: "+vehicle.inTrip);
	                        			}
	                        			if(locationJSON.has("last_trip_id")){
	                        				vehicle.lastTripId = locationJSON.optLong("last_trip_id");	
	                        				System.out.println("last_trip_id: "+locationJSON.optLong("last_trip_id"));								
	                        			}
	                        			if(locationJSON.has("last_trip_time")){
	                        				vehicle.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString("last_trip_time"));										
	                        			}
	                        		} catch (JSONException ex) {
	                        			ex.printStackTrace();
	                        		}
	            				}

	            				if(extras.containsKey("updated_at")){
	            					vehicle.updatedAt = Utils.fixTimezoneZ(extras.getString("updated_at"));
	            				}
	            				dbHelper.saveVehicle(vehicle);
							}
            				
            				dbHelper.close();
            				
            				sendBroadcast(new Intent(Constants.BROADCAST_UPDATE_VEHICLES));
            			}
            			
            		case ACTION_NEW_TRIP:
            			long vehicleId = 0;
            			if(extras.containsKey("vehicle_id")){
            				vehicleId = Long.parseLong(extras.getString("vehicle_id"));
            				Intent i = new Intent(Constants.BROADCAST_UPDATE_TRIPS);
            				i.putExtra("vehicle_id", vehicleId);
            				sendBroadcast(i);
        				}
            			
            			long tripId = extras.containsKey("trip_id") ? Long.parseLong(extras.getString("trip_id")) : 0;
            			
            			if(vehicleId!=0 && tripId!=0){
            				notificationIntent = new Intent(getApplicationContext(), TripActivity.class);
            				notificationIntent.putExtra(TripActivity.EXTRA_VEHICLE_ID, vehicleId);
            				notificationIntent.putExtra(TripActivity.EXTRA_TRIP_ID, tripId);
            			}
            			break;
            			
            		default:
            			break;
            		}
            	}
            	
            	if(extras.containsKey("aps")){
            		String apsJSON = extras.getString("aps");
            		try {
            			JSONObject jsonObj = new JSONObject(apsJSON);
            			String message = jsonObj.optString("alert");
            			String sound = jsonObj.optString("sound");
            			if(!TextUtils.isEmpty(message))
            				sendNotification(getResources().getString(R.string.app_name), message, TextUtils.isEmpty(sound) ? false : true, notificationIntent);
            			
            		} catch (JSONException e1) {
            			e1.printStackTrace();
            		}
            	}
            	
            	new GetDeviceInfoRequest(this).execute();
            }
        }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    private boolean isNull(String value){
    	if(value == null || value.equals("") || value.equals("null"))
    		return true;
    	else
    		return false;
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg, boolean sound, Intent intent) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
        .setAutoCancel(true)
        .setTicker(msg)
        .setContentText(msg)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        
        if(intent!=null){
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        	mBuilder.setContentIntent(contentIntent);
        }
        
        Notification n = mBuilder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		if(sound){
			n.defaults |= Notification.DEFAULT_SOUND;
			n.defaults |= Notification.DEFAULT_VIBRATE;
		}
        mNotificationManager.notify(NOTIFICATION_ID, n);
    }
}
