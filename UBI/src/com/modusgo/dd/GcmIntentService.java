package com.modusgo.dd;

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
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modusgo.ubi.InitActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.utils.Device;

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
            	
            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            	Editor e  = prefs.edit();
            	
            	
//            	for (String key : extras.keySet()) {
//            	    Object value = extras.get(key);
//            	    Log.d(TAG, String.format("%s %s (%s)", key,  
//            	        value.toString(), value.getClass().getName()));
//            	}
            	
            	Bugsnag.addToTab("User", "Push data", extras.toString());
            	
            	String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
            	
            	if(deviceType.equals(Device.DEVICE_TYPE_OBD)){
	            	Bugsnag.notify(new RuntimeException("Push received"));
	            	
	            	if(extras.containsKey("aps")){
						String apsJSON = extras.getString("aps");
						try {
							JSONObject jsonObj = new JSONObject(apsJSON);
							if(jsonObj.optInt("content-available")==0){
								String message = jsonObj.optString("alert");
				            	if(!TextUtils.isEmpty(message))
				            		sendNotification(getResources().getString(R.string.app_name), message);
							}
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
	            	}
	            	
	            	if(extras.containsKey("in_trip")){
						e.putBoolean(Device.PREF_DEVICE_IN_TRIP, !TextUtils.isEmpty(extras.getString("in_trip")));
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
	            	
	            	e.commit();
            	}
            	else{
	            	Bugsnag.notify(new RuntimeException("Push received, not proccessed, device is not OBD"));
            	}
            	
            	new GetDeviceInfoRequest(this).execute();
            }
        }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, InitActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
        .setAutoCancel(true)
        .setTicker(msg)
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        Notification n = mBuilder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, n);
    }
}
