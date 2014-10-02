package com.modusgo.dd;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.modusgo.dd.requests.CheckIgnitionRequest;
import com.modusgo.dd.requests.SendStatsRequest;
import com.modusgo.demo.R;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.SignInActivity;
import com.modusgo.ubi.utils.Utils;

public class CellBlockerService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

	public CellBlockerService() {
		super();
	}
	
	private Handler handler;
	Runnable runnable;
	private Handler checkIgnitionHandler;
	private Runnable checkIgnitionRunnable;
	ArrayList<String> whitelist;
	ArrayList<String> blacklist;
	
	//private boolean smsReceiverRegistered = false;
	//private boolean callReceiverRegistered = false;
	
	private int notificationId = 7;
	private NotificationManager notificationManager;
	
	private SharedPreferences prefs;
	
	//Location stuff
	private Boolean servicesAvailable = false;
    private boolean mInProgress;
	LocationClient mLocationClient;
	Location mLastLocation;
	private long lastIgnitionReceivedTime = 0;
    
	
	@Override
	public void onCreate() {
		super.onCreate();		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        servicesAvailable = servicesConnected();
		mInProgress = false;
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("ModusGO")
		        .setContentText("ModusGO tracking is "+(prefs.getBoolean(Constants.PREF_DD_ENABLED, false) ? "enabled." : "disabled."));

		Intent resultIntent = new Intent(this, SignInActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);//stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(notificationId, n);
		
		if(handler!=null) 
			handler.removeCallbacks(runnable);
		if(checkIgnitionHandler!=null) 
			checkIgnitionHandler.removeCallbacks(checkIgnitionRunnable);
		lastIgnitionReceivedTime = 0;
				
		setUpLocationClientIfNeeded();
        if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
        {
        	mInProgress = true;
        	mLocationClient.connect();
        }

		System.out.println("service start");
		
		checkIgnitionHandler = new Handler();
	    checkIgnitionRunnable = new Runnable() {
	        @Override
	        public void run() {
	        	
	        	if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
	        		
	        		System.out.println("runnable start");
		        	
		        	if(lastIgnitionReceivedTime==0)
		        		lastIgnitionReceivedTime = System.currentTimeMillis();
		        	
		        	if(prefs.getBoolean(Constants.PREF_DD_ENABLED, true) && lastIgnitionReceivedTime!= 0 && System.currentTimeMillis()-lastIgnitionReceivedTime>Constants.CHECK_IGNITION_NOT_AVAILABLE_TIME_LIMIT){
		        		setBlockEnabled(false);
		        	}
		        	
		        	if(mLocationClient!=null && mLocationClient.isConnected()){
			        	mLastLocation = mLocationClient.getLastLocation();
			        	if(mLastLocation!=null){
				            new CheckIgnitionRequest(CellBlockerService.this, mLastLocation.getLongitude(), mLastLocation.getLatitude()){
				            	@Override
				            	protected void onPostExecuteSuccess(String ignition, String awayStr, String meters) {
				            		
				            		lastIgnitionReceivedTime = System.currentTimeMillis();
				            		
				            		boolean blockEnabled = Integer.parseInt(ignition)==0 ? false : true; 
				            		//boolean away = Integer.parseInt(awayStr)==0 ? false : true;
				            		//int distance =  Integer.parseInt(meters);
		
				            		if((!blockEnabled/* || away*/) && prefs.getBoolean(Constants.PREF_DD_ENABLED, false)){
				            			/*if(away){
				            				Toast.makeText(getApplicationContext(), "Protection disabled, phone is " + distance + " meters away from your car.", Toast.LENGTH_SHORT).show();
				            				if(mLastLocation!=null){
				            					if(!HockeyCrashManager.DEBUG){
				            						Bugsnag.register(CellBlockerService.this, Constants.BUGSNAG_KEY);
				            						Bugsnag.addToTab("Phone Away", "Last location accuracy", mLastLocation.getAccuracy());
				            						Bugsnag.addToTab("Phone Away", "Ignition", ignition);
				            						Bugsnag.addToTab("Phone Away", "Away", awayStr);
				            						Bugsnag.addToTab("Phone Away", "Distance", meters);
				            						Bugsnag.notify(new RuntimeException("Phone away from device"));
				            					}
				            				}
				            			}*/
				            			setBlockEnabled(false);
				            		}
				            		else if(blockEnabled && !prefs.getBoolean(Constants.PREF_DD_ENABLED, false)){
				            			setBlockEnabled(true);				            			
				            		}
				            		super.onPostExecuteSuccess(ignition, awayStr, meters);
				            	}
				            }.execute();
			        	}
		        	}
		            checkIgnitionHandler.postDelayed(this, Constants.CHECK_IGNITION_FREQUENCY);
	        	}
	        }
	    };
	    checkIgnitionHandler.postDelayed(checkIgnitionRunnable, Constants.CHECK_IGNITION_FREQUENCY);
		
		if(!servicesAvailable || mLocationClient.isConnected() || mInProgress)
        	return START_STICKY;
		
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
	private void setBlockEnabled(boolean enabled){
		prefs.edit().putBoolean(Constants.PREF_DD_ENABLED, enabled).commit();
		Intent i = new Intent(getApplicationContext(), CellBlockerService.class);
		startService(i);
		
		if(!enabled)
			new SendStatsRequest(this).execute(Constants.getSendStatisticsURL(Utils.getUUID(this)));
		
		Intent i2 = new Intent(Constants.INTENT_ACTION_UPDATE_MAIN);
	    sendBroadcast(i2);    	
    }
	
	@Override
	public void onDestroy() {
		/*if(smsReceiverRegistered){
			unregisterReceiver(incomingSMSReceiver);
			smsReceiverRegistered = false;
		}
		if(callReceiverRegistered){
			unregisterReceiver(incomingCallReceiver);
			callReceiverRegistered = false;
		}*/
		
		if(handler!=null) handler.removeCallbacks(runnable);
		notificationManager.cancel(notificationId);
		
		mInProgress = false;
        if(servicesAvailable && mLocationClient != null) {
	        //mLocationClient.removeLocationUpdates(this);
	        // Destroy the current location client
	        mLocationClient = null;
        }
		
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean servicesConnected(/*ConnectionResult connectionResult*/) {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            // Get the error code
            //int errorCode = connectionResult.getErrorCode();
            return false;
        }
    }
	
	private void setUpLocationClientIfNeeded()
    {
    	if(mLocationClient == null) 
            mLocationClient = new LocationClient(this, this, this);
    }
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
    	
        // Request location updates using static settings
        //mLocationClient.requestLocationUpdates(mLocationRequest, this);
        
    }
 
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
 
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	mInProgress = false;
    	
        if (connectionResult.hasResolution()) {

        } else {
 
        }
    }
}
