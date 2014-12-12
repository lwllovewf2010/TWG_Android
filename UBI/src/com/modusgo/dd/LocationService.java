package com.modusgo.dd;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.InitActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.Tracking;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.utils.Device;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

	private static final float TRIP_START_SPEED = 7f/3.6f;
	private static final float TRIP_STAY_SPEED = 3f/3.6f;
	private static final int MAX_STAY_TIME = 2*60*1000;
	public static final int GET_DEVICE_FREQUENCY = 60*60*1000;
	
	private long stayFromMillis = 0;
	
	public LocationService() {
		super();
	}
	
	private Handler checkIgnitionHandler;
	private Runnable checkIgnitionRunnable;
	
	PhoneScreenOnOffReceiver receiver;
	//private boolean smsReceiverRegistered = false;
	//private boolean callReceiverRegistered = false;
	
	private int notificationId = 7;
	
	private SharedPreferences prefs;
	
	//Location stuff
	private Boolean servicesAvailable = false;
    private boolean mInProgress;
	private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    
	
	@Override
	public void onCreate() {
		super.onCreate();		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(5000);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(1000);
        
        servicesAvailable = servicesConnected();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String mode = prefs.getString(Device.PREF_CURRENT_TRACKING_MODE, "");
		
		switch (mode) {
		case Device.MODE_LIGHT_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
	        mLocationRequest.setSmallestDisplacement(50);
	        mLocationRequest.setInterval(30*60*1000);
	        mLocationRequest.setFastestInterval(30*60*1000);

			break;
		case Device.MODE_SIGNIFICATION_TRACKING:

			break;
		case Device.MODE_MEDIUM_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(50);
	        mLocationRequest.setInterval(5*60*1000);
	        mLocationRequest.setFastestInterval(1*60*1000);
			break;
		case Device.MODE_NAVIGATION_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(10);
	        mLocationRequest.setInterval(10*1000);
	        mLocationRequest.setFastestInterval(10*1000);
			break;
		case Device.MODE_SUPER_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(5);
	        mLocationRequest.setInterval(1*1000);
	        mLocationRequest.setFastestInterval(1*1000);
			break;

		default:
			break;
		}
		
		if(mLocationClient!=null && mLocationClient.isConnected()){
			mLocationClient.removeLocationUpdates(this);
			if(!mode.equals(Device.MODE_SIGNIFICATION_TRACKING))
				mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
		
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getResources().getString(R.string.app_name))
		        .setContentText("Your trip is "+(TextUtils.isEmpty(prefs.getString(Device.PREF_DEVICE_IN_TRIP, "")) ? "stopped." : "in progress."));

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
		        	new GetDeviceInfoRequest(LocationService.this).execute();
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
		
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
	@Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        System.out.println(msg);
        
        Editor e = prefs.edit();
        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+location.getLatitude());
        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+location.getLongitude());
        
        String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		
        boolean inTrip = prefs.getBoolean(Device.PREF_DEVICE_IN_TRIP, false);
        String event = "";
    	if(!inTrip){
    		if(deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) && location.getSpeed()>TRIP_START_SPEED){
            	e.putBoolean(Device.PREF_DEVICE_IN_TRIP, true);
    			event = "start";
            }
    	}
    	else{
    		if((deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) || deviceType.equals(Device.DEVICE_TYPE_IBEACON)) && location.getSpeed() < TRIP_STAY_SPEED){
	    		if(stayFromMillis==0)
	    			stayFromMillis = System.currentTimeMillis();
	    		else if(System.currentTimeMillis() - stayFromMillis > MAX_STAY_TIME){
	    			stayFromMillis = 0;
	    			e.putBoolean(Device.PREF_DEVICE_IN_TRIP, false);
	    			event = "stop";
	    		}
    		}
    	}
    	

    	DbHelper dbhelper = DbHelper.getInstance(this);
    	dbhelper.saveTrackingEvent(new Tracking(
    			location.getTime(), 
    			location.getLatitude(), 
    			location.getLongitude(), 
    			location.getAltitude(), 
    			location.getBearing(), 
    			location.getAccuracy(), 
    			0, 
    			0, 
    			true, 
    			location.getSpeed(), 
    			event, 
    			""));
    	dbhelper.close();
    	
    	e.commit();
        Device.checkDevice(this);
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
		
		mInProgress = false;
        if(servicesAvailable && mLocationClient != null) {
        	if(mLocationClient.isConnected())
        		mLocationClient.removeLocationUpdates(this);
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
    	mLocationClient.requestLocationUpdates(mLocationRequest, this);
    	
    	Location lastLocation = mLocationClient.getLastLocation();
    	Editor e = prefs.edit();
        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+lastLocation.getLatitude());
        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+lastLocation.getLongitude());
        e.commit();
        
        System.out.println("Location service Connected");
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
    	System.out.println(connectionResult.toString());
    	
        if (connectionResult.hasResolution()) {

        } else {
 
        }
    }
}
