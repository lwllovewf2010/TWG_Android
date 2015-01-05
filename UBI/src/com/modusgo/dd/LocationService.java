package com.modusgo.dd;

import java.util.List;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bugsnag.MetaData;
import com.bugsnag.android.Bugsnag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.kontakt.sdk.android.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.configuration.MonitorPeriod;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.device.Beacon;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.factory.Filters;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.logentries.android.AndroidLogger;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.InitActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.Tracking;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.utils.Device;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

	private static final float TRIP_START_SPEED = 15f/3.6f;
	private static final float TRIP_STAY_SPEED = 10f/3.6f;
	private static final int MAX_STAY_TIME = 2*60*1000;
	
	public static final int GET_DEVICE_FREQUENCY = 60*60*1000;
	
	public static final int MIN_ACCURACY = 80;
	
	private long stayFromMillis = 0;
	private long lastLocationUpdateTime = 0;
	
	public LocationService() {
		super();
	}
	
	private Handler checkIgnitionHandler;
	private Runnable checkIgnitionRunnable;

	private Handler checkLocationUpdatesHandler;
	private Runnable checkLocationUpdatesRunnable;
	private int checkLocationUpdatesFrequency = 90*1000;
	
	PhoneScreenOnOffReceiver phoneScreenOnOffReceiver;
	//private boolean smsReceiverRegistered = false;
	//private boolean callReceiverRegistered = false;
	
	private int notificationId = 7;
	
	private SharedPreferences prefs;
	
	//Location stuff
	private Boolean servicesAvailable = false;
    private boolean mInProgress;
	private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    
    private BeaconManager beaconManager;
    private boolean beaconConnected = false;
    
    AndroidLogger logger;
	
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
        
        logger = AndroidLogger.getLogger(getApplicationContext(), "561a64f6-9d58-4ff3-ab25-a932ff2d10c6", false);

		Bugsnag.addToTab("User", "E-mail", prefs.getString(Constants.PREF_EMAIL, "not specified"));
		Bugsnag.addToTab("User", "Device Type", prefs.getString(Device.PREF_DEVICE_TYPE, "not specified"));
		Bugsnag.addToTab("User", "Device MEID", prefs.getString(Device.PREF_DEVICE_MEID, "not specified"));
		Bugsnag.addToTab("User", "DD Events", prefs.getBoolean(Device.PREF_DEVICE_EVENTS, false));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
			updateServiceMode();
			updateNotification();
			
			setUpLocationClientIfNeeded();
	        if(!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress)
	        {
	        	mInProgress = true;
	        	mLocationClient.connect();
	        }
			
	        if(checkIgnitionHandler==null){
	        	checkIgnitionHandler = new Handler();
	        }
	        
	        if(checkIgnitionRunnable==null){
				checkIgnitionHandler.removeCallbacks(checkIgnitionRunnable);
	        	checkIgnitionRunnable = new Runnable() {
	    	        @Override
	    	        public void run() {
	    	        	
	    	        	if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
	    		        	new GetDeviceInfoRequest(LocationService.this).execute();
	    		        	updateNotification();
	    		            checkIgnitionHandler.postDelayed(this, GET_DEVICE_FREQUENCY);
	    	        	}
	    	        }
	    	    };
	    	    checkIgnitionHandler.postDelayed(checkIgnitionRunnable, 0);
	        }
	        
	        if(checkLocationUpdatesHandler==null){
				checkLocationUpdatesHandler = new Handler();
	        }
	        
	        if(checkLocationUpdatesRunnable==null){
	        	checkLocationUpdatesHandler.removeCallbacks(checkLocationUpdatesRunnable);
				checkLocationUpdatesRunnable = new Runnable() {
	    	        @Override
	    	        public void run() {
	    	        	System.out.println("checkLocationUpdatesRunnable");
	    	        	if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
		    	        	if(System.currentTimeMillis() - lastLocationUpdateTime > MAX_STAY_TIME){
		    	        		Bugsnag.notify(new RuntimeException("Trip stop, no location for 2 minutes"));
		    	        		System.out.println("Trip stop, no location for 2 minutes");
		    	        		savePoint("stop");
		    	        		checkLocationUpdatesHandler.removeCallbacks(checkLocationUpdatesRunnable);
		    	        	}
		    	        	else{
			    	        	System.out.println("checkLocationUpdatesRunnable reposted");
		    	        		checkLocationUpdatesHandler.postDelayed(this, checkLocationUpdatesFrequency);
		    	        	}
	    	        	}
	    	        }
	    	    };
	        }
		    
		    if(phoneScreenOnOffReceiver==null){
			    phoneScreenOnOffReceiver = new PhoneScreenOnOffReceiver();
			    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			    filter.addAction(Intent.ACTION_SCREEN_OFF);
			    filter.addAction(Intent.ACTION_SCREEN_OFF);
			    registerReceiver(phoneScreenOnOffReceiver, filter);
		    }
		}
		else
			stopSelf();
	    
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
	private void iBeaconConnect() {
        try {
        	System.out.println("ibeacon connect");
            beaconManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() {
                    try {
                    	System.out.println("ibeacon start monitoring");
                        beaconManager.startMonitoring();
                    	
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (RemoteException e) {
//        	throw new IllegalStateException(e);
        	e.printStackTrace();
        }
    }
	
	private void updateServiceMode(){
		String mode = prefs.getString(Device.PREF_CURRENT_TRACKING_MODE, "");
		System.out.println("Mode: "+mode);
		
		if(prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_IBEACON)){
			if(beaconManager==null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
		    	beaconManager = BeaconManager.newInstance(this);
		    	beaconManager.setMonitorPeriod(MonitorPeriod.MINIMAL);
		    	beaconManager.setForceScanConfiguration(ForceScanConfiguration.DEFAULT);
		    	System.out.println("ubi ibeacon init");
	        	System.out.println("ibeacon set filter : "+prefs.getString(Device.PREF_DEVICE_MEID, ""));
	        	String beaconId = prefs.getString(Device.PREF_DEVICE_MEID, "");
	        	if(beaconId.length()>4)
	        		beaconManager.addFilter(Filters.newProximityUUIDFilter(UUID.fromString(beaconId)));
	        	else
	        		beaconManager.addFilter(Filters.newBeaconUniqueIdFilter(beaconId));
		    	beaconManager.registerMonitoringListener(new BeaconManager.MonitoringListener() {
		    		@Override
		    		public void onMonitorStart() {
		    			System.out.println("ibeacon monitor start");
		    		}
		    		
		    		@Override
		    		public void onMonitorStop() {
		    			System.out.println("ibeacon monitor stop");
		    		}
		    		
		    		@Override
		    		public void onBeaconsUpdated(final Region region, final List<Beacon> beacons) {
		    			System.out.println("ibeacons updated, "+beacons.size()+" beacon connected: "+beaconConnected+" trip in process: "+prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false));
		    			if(!beaconConnected || !prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
			    			beaconConnected = true;
			    			if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
			    				savePoint("connected");
			    			}
			    			else{
			    				savePoint("start");
			    			}
		    			}
		    		}
		    		
		    		@Override
		    		public void onBeaconAppeared(final Region region, final Beacon beacon) {
		    			System.out.println("ibeacon appeared, "+beacon.getBeaconUniqueId());
		    		}
		    		
		    		@Override
		    		public void onRegionEntered(final Region region) {
		    			System.out.println("ibeacon region entered, bc: "+beaconConnected);
		    			if(!beaconConnected || !prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
			    			beaconConnected = true;
			    			if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
			    				savePoint("connected");
			    			}
			    			else{
			    				savePoint("start");
			    			}
		    			}
		    		}
		    		
		    		@Override
		    		public void onRegionAbandoned(final Region region) {
		    			System.out.println("ibeacon region abdandoned");
		    			if(beaconConnected){
			    			beaconConnected = false;
			    			savePoint("disconnected");
		    			}
		    		}
		    	});
		    	iBeaconConnect();
			}
	    }
		else{
			if(beaconManager!=null){
				System.out.println("ibeacon monitoring stop on update");
				beaconManager.stopMonitoring();
			}
		}
		
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
	}
	
	private void updateNotification(){
		
		String servicesToEnable = "";
		int servicesToEnableCount = 0;
		
		if(prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_OBD)){
			if(prefs.getBoolean(Device.PREF_DEVICE_EVENTS, false)){
				if(!isLocationServicesEnabled()){
			    	servicesToEnable += "Location services";
			    	servicesToEnableCount++;
				}
			}
		}
		else{
			if(!isLocationServicesEnabled()){
		    	servicesToEnable += "Location services";
		    	servicesToEnableCount++;
			}
		}
		
		if(prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_IBEACON) || prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_OBDBLE)){
			if(!isBluetoothServicesEnabled()){
				servicesToEnable += servicesToEnableCount > 0 ? ", " : "";
		    	servicesToEnable += "Bluetooth service";
		    	servicesToEnableCount++;
			}
		}

		if(servicesToEnableCount>0){
	    	showNotification("Please, enable next service" + (servicesToEnableCount > 1 ? "s" : "") + " for better experience: "+servicesToEnable, new Intent(android.provider.Settings.ACTION_SETTINGS));
		}
		
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getResources().getString(R.string.app_name)) 
		        .setContentText("Your trip is " + ((prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false) ? "in progress." : "stopped.") + "Mode: " + prefs.getString(Device.PREF_CURRENT_TRACKING_MODE, "none")));

		Intent resultIntent = new Intent(this, InitActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);//stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		
		startForeground(notificationId, n);
	}
	
	private boolean isLocationServicesEnabled(){
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		try {
			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
				return true;
			else
				return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	private boolean isBluetoothServicesEnabled(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			return false;
		} else {
		    if (mBluetoothAdapter.isEnabled())
		    	return true;
		    else
		    	return false;
		}
	}
	
	private void showNotification(String message, Intent resultIntent){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getString(R.string.app_name))
		        .setContentText(message)
		        .setAutoCancel(true);
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,Intent.FLAG_ACTIVITY_NEW_TASK);
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(29, mBuilder.build());
	}
	
	private void updateLocationUpdatesHandler(){
		final String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		if(checkLocationUpdatesHandler!=null && checkLocationUpdatesRunnable!=null){
			checkLocationUpdatesHandler.removeCallbacks(checkLocationUpdatesRunnable);
			if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false) && (deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) || (deviceType.equals(Device.DEVICE_TYPE_IBEACON) && !beaconConnected))){
				System.out.println("check location updates posted");
		    	checkLocationUpdatesHandler.postDelayed(checkLocationUpdatesRunnable, 0);
			}
			else{
				System.out.println("check location updates removed");
			}
		}
	}
	
	@Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		
        boolean deviceInTrip = prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false);
        String event = "";
        
        if(deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) && !deviceInTrip && location.getSpeed()>TRIP_START_SPEED){
        	event = "start";
        }
        
        if((deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) || (deviceType.equals(Device.DEVICE_TYPE_IBEACON) && !beaconConnected)) && deviceInTrip){
        	
        	if(location.getSpeed() < TRIP_STAY_SPEED){
        		if(stayFromMillis==0)
	    			stayFromMillis = System.currentTimeMillis();
	    		else if(System.currentTimeMillis() - stayFromMillis > MAX_STAY_TIME){
	    			event = "stop";
	    		}
        	}
        	else {
        		stayFromMillis = 0;
			}
        }
        
        lastLocationUpdateTime = System.currentTimeMillis();
        savePoint(location, event);
    }
	
	private void savePoint(Location location, String event){

        String msg ="point " + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()) + 
                ", accuracy: "+location.getAccuracy() + 
                ", speed: " + (location.getSpeed()*3.6f) + 
                ", saved: " + (location.getAccuracy() <= MIN_ACCURACY) + 
                ", event: "+event;
        
        System.out.println(msg);
        logger.info(msg);
        
        if(location.getAccuracy() <= MIN_ACCURACY){
	        
	        if(event.equals("")){
	        	//Bugsnag.addToTab("User", "Point data", msg);
	        }
	        else{
	        	Bugsnag.notify(new RuntimeException(msg));
	        }
			
			Editor e = prefs.edit();
	        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+location.getLatitude());
	        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+location.getLongitude());
	        
			if(event.equals("start")){
				e.putBoolean(Device.PREF_IN_TRIP_NOW, true).commit();
	    		stayFromMillis = 0;
	    		lastLocationUpdateTime = System.currentTimeMillis();
	        	updateLocationUpdatesHandler(); 
			}
			else if(event.equals("stop"))
				e.putBoolean(Device.PREF_IN_TRIP_NOW, false).commit();
			else
				e.commit();
	    	
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
	    			""), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
	    	dbhelper.close();
	    	
	    	if(!TextUtils.isEmpty(event))
	    		updateNotification();
	    	   	
	        Device.checkDevice(this);
        }
	}
	
	private void savePoint(String event){

        String msg = "point event: "+event;
        System.out.println(msg);
        logger.info(msg);
		
        Editor e = prefs.edit();
		if(event.equals("start")){
			e.putBoolean(Device.PREF_IN_TRIP_NOW, true).commit();
    		stayFromMillis = 0;
    		lastLocationUpdateTime = System.currentTimeMillis();
        	updateLocationUpdatesHandler(); 			
		}
		else if(event.equals("stop"))
			e.putBoolean(Device.PREF_IN_TRIP_NOW, false).commit();
    	
		DbHelper dbhelper = DbHelper.getInstance(this);
    	dbhelper.saveTrackingEvent(new Tracking(
    			System.currentTimeMillis(), 
    			0, 
    			0, 
    			0, 
    			0, 
    			0, 
    			0, 
    			0, 
    			true, 
    			0, 
    			event, 
    			""), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
    	dbhelper.close();

		Bugsnag.notify(new RuntimeException("event: "+event));
    	updateNotification();  	
        Device.checkDevice(this);
	}
	
	@Override
	public void onDestroy() {
		System.out.println("service destroy ");
		
		if(phoneScreenOnOffReceiver!=null)
			unregisterReceiver(phoneScreenOnOffReceiver);
		
		if(checkLocationUpdatesHandler!=null && checkLocationUpdatesRunnable!=null){
			checkLocationUpdatesHandler.removeCallbacks(checkLocationUpdatesRunnable);
		}
		
		if(checkIgnitionHandler!=null && checkIgnitionRunnable!=null){
			checkIgnitionHandler.removeCallbacks(checkIgnitionRunnable);
		}
		
		if(beaconManager!=null){
			System.out.println("ibeacon disconnect ");
			beaconManager.stopMonitoring();
			beaconManager.disconnect();
	        beaconManager = null;
		}
		
		mInProgress = false;
        if(servicesAvailable && mLocationClient != null) {
        	if(mLocationClient.isConnected())
        		mLocationClient.removeLocationUpdates(this);
	        // Destroy the current location client
	        mLocationClient = null;
        }
        
        if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
        	Bugsnag.notify(new RuntimeException("Trip stop, service destroyed"));
        	System.out.println("Trip stop, service destroyed");
        	savePoint("stop");
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
    	
		updateServiceMode();
    	
    	Location lastLocation = mLocationClient.getLastLocation();
    	if(lastLocation!=null){
	    	Editor e = prefs.edit();
	        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+lastLocation.getLatitude());
	        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+lastLocation.getLongitude());
	        e.commit();
    	}
        lastLocationUpdateTime = System.currentTimeMillis();
        
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
