package com.modusgo.dd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.InitActivity;
import com.farmers.ubi.R;
import com.modusgo.ubi.utils.Utils;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.utils.Device;

public class TrackingStatusService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{

	public static final int GET_DEVICE_FREQUENCY = 60*60*1000;
	
	public TrackingStatusService() {
		super();
	}
	
	private Handler checkIgnitionHandler;
	private Runnable checkIgnitionRunnable;
	
	PhoneScreenOnOffReceiver receiver;
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
		        .setContentText("ModusGO tracking is "+(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false) ? "enabled." : "disabled."));

		Intent resultIntent = new Intent(this, InitActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);//stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		
		startForeground(notificationId, n);
		
		if(checkIgnitionHandler!=null) 
			checkIgnitionHandler.removeCallbacks(checkIgnitionRunnable);
				
		setUpLocationClientIfNeeded();
        if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
        {
        	mInProgress = true;
        	mLocationClient.connect();
        }
		
		checkIgnitionHandler = new Handler();
	    checkIgnitionRunnable = new Runnable() {
	        @Override
	        public void run() {
	        	
	        	if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
		        	
		        	if(mLocationClient!=null && mLocationClient.isConnected()){
			        	mLastLocation = mLocationClient.getLastLocation();
			        	if(mLastLocation!=null){
			        		
			        	}
		        	}
		        	
		        	new GetDeviceInfoRequest(TrackingStatusService.this).execute();
		        	
		            checkIgnitionHandler.postDelayed(this, GET_DEVICE_FREQUENCY);
	        	}
	        }
	    };
	    checkIgnitionHandler.postDelayed(checkIgnitionRunnable, GET_DEVICE_FREQUENCY);
	    
	    receiver = new PhoneScreenOnOffReceiver();
	    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);
	    filter.addAction(Intent.ACTION_SCREEN_OFF);
	    registerReceiver(receiver, filter);
		
		if(!servicesAvailable || mLocationClient.isConnected() || mInProgress)
        	return START_STICKY;
		
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
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
		
		unregisterReceiver(receiver);
		
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
