package com.modusgo.dd;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modusgo.dd.requests.SendStatsRequest;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.MainActivity;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
//    public static final int NOTIFICATION_ID = 1;
//    private NotificationManager mNotificationManager;
//    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    
    public static final String TAG = "DistractedDriving GCM";
    
    private static final String MESSAGE_EVENT = "event";
    private static final String MESSAGE_IGNITION = "ignition";
    
    private static final String EVENT_IGNITION = "ignition";
    private static final String EVENT_UNREGISTER = "unregister";
    

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
                
                // Post notification of received message.
                //sendNotification("Received: " + extras.toString());
                SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                
                //если пользователь зарегистрирован
                if(!prefs.getString(Constants.PREF_REG_CODE, "").equals("") || !prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
	                //тогда парсить сообщения gcm
                	String eventMessage = extras.getString(MESSAGE_EVENT);
                	if(eventMessage!=null){
                		if(eventMessage.equals(EVENT_IGNITION)){
                			if(extras.getString(MESSAGE_IGNITION).equals("1"))
                				setBlockEnabled(true);
                			else{
                				setBlockEnabled(false);
                				new SendStatsRequest(getApplicationContext()).execute(Constants.getSendStatisticsURL(Secure.getString(getContentResolver(), Secure.ANDROID_ID)));
                			}
                		}
                		else if(eventMessage.equals(EVENT_UNREGISTER)){
                			if(prefs.getBoolean(Constants.PREF_DD_ENABLED, false)){
    	                		setBlockEnabled(false);
    	                	}
    	                	
    	                	Editor editor = prefs.edit();
    						editor.putString(Constants.PREF_REG_CODE, "");
    	    				editor.commit();
    	    				//Clear GCM preferences file
    	    				getSharedPreferences(MainActivity.class.getSimpleName(),Context.MODE_PRIVATE).edit().clear().commit();
    	    				stopService(new Intent(this, TrackingStatusService.class));
    	    				//Toast.makeText(getApplicationContext(), "Distracted Driving: device unregistered", Toast.LENGTH_SHORT);
    	    				//sendNotification("Distracted Driving", "Device unregistered, tap to reregister.");
                		}
                	}
                }
                else{
                }
            }
        }
        
        Intent i = new Intent(Constants.INTENT_ACTION_UPDATE_MAIN);
        sendBroadcast(i);
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    
    private void setBlockEnabled(boolean enabled){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putBoolean(Constants.PREF_DD_ENABLED, enabled).commit();
		
		Intent i = new Intent(getApplicationContext(), TrackingStatusService.class);
		startService(i); 
    	
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
//    private void sendNotification(String title, String msg) {
//        mNotificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, UserRegisterActivity.class), 0);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//        .setSmallIcon(R.drawable.ic_launcher)
//        .setContentTitle(title)
//        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
//        .setAutoCancel(true)
//        .setTicker(msg)
//        .setContentText(msg);
//
//        mBuilder.setContentIntent(contentIntent);
//        Notification n = mBuilder.build();
//        //n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
//        mNotificationManager.notify(NOTIFICATION_ID, n);
//    }
}
