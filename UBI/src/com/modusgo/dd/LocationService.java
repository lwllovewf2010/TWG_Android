package com.modusgo.dd;

import java.util.List;

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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kontakt.sdk.android.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.factory.AdvertisingPackage;
import com.kontakt.sdk.android.factory.Filters.CustomFilter;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.kontakt.sdk.android.manager.BeaconManager.MonitoringListener;
import com.logentries.android.AndroidLogger;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.InitActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.Tracking;
import com.modusgo.ubi.TripDeclineActivity;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.jastec.JastecManager;
import com.modusgo.ubi.jastec.JastecManager.OnSensorListener;
import com.modusgo.ubi.jastec.LogActivity;
import com.modusgo.ubi.requesttasks.GetDeviceInfoRequest;
import com.modusgo.ubi.requesttasks.SendEventsRequest;
import com.modusgo.ubi.utils.Device;

public class LocationService extends Service implements ConnectionCallbacks,
OnConnectionFailedListener,
LocationListener{

	private static final int PERMANENT_NOTIFICATION_ID = 30;
	private static final int SERVICES_DISABLED_NOTIFICATION_ID = 29;
	private static final int TRIP_DECLINE_NOTIFICATION_ID = 28;

	private static final float TRIP_START_SPEED = 15f/3.6f;
	private static final float TRIP_STAY_SPEED = 10f/3.6f;
	private static final int MAX_STAY_TIME = 2*60*1000;
	private static final int MAX_BEACON_NO_CONNECTION_STAY_TIME = 10*1000;
	
	public static final int GET_DEVICE_FREQUENCY = 60*60*1000;
	
	public static final int MIN_ACCURACY = 40;
	
	public static final String EXTRA_ACTION = "extraAction";
	public static enum Action {TRIP_DECLINE}
	
	private long stayFromMillis = 0;
	private long lastLocationUpdateTime = 0;
	
	public LocationService() {
		super();
	}
	
	private Handler checkIgnitionHandler;
	private Runnable checkIgnitionRunnable;

	private Handler checkLocationUpdatesHandler;
	private Runnable checkLocationUpdatesRunnable;
	private int checkLocationUpdatesFrequency = 5*1000;
	
	private Handler tripDeclineNotificationHandler = new Handler();
	
	private Handler jastecHandler;
	private Runnable jastecRunnable;
	
	PhoneScreenOnOffReceiver phoneScreenOnOffReceiver;
	//private boolean smsReceiverRegistered = false;
	//private boolean callReceiverRegistered = false;
	
	private SharedPreferences prefs;
	
	//Location stuff
	private Boolean servicesAvailable = false;
    private boolean mInProgress;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public final static String EVENT_TRIP_START = "start";
    public final static String EVENT_TRIP_STOP = "stop";
    private final static String EVENT_BEACON_CONNECTED = "connected";
    private final static String EVENT_BEACON_DISCONNECTED = "disconnected";
    public final static String EVENT_TRIP_DECLINED = "declined";

    private static final double ACCEPT_DISTANCE = 4;//[m]
    private BeaconManager beaconManager;
    private boolean beaconConnected = false;
    private long lastBeaconDisconnectMillis;
    
    private JastecManager jastecMan;
    private OnSensorListener jastecTripStateListener;
    private long lastJastecPing;
    
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
        
        logger = AndroidLogger.getLogger(getApplicationContext(), "561a64f6-9d58-4ff3-ab25-a932ff2d10c6", false);

		Bugsnag.addToTab("User", "E-mail", prefs.getString(Constants.PREF_EMAIL, "not specified"));
		Bugsnag.addToTab("User", "Device Type", prefs.getString(Device.PREF_DEVICE_TYPE, "not specified"));
		Bugsnag.addToTab("User", "Device MEID", prefs.getString(Device.PREF_DEVICE_MEID, "not specified"));
		Bugsnag.addToTab("User", "DD Events", prefs.getBoolean(Device.PREF_DEVICE_EVENTS, false));
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
			
			Action action = intent != null ? (Action) intent.getSerializableExtra(EXTRA_ACTION) : null;
			
			if(action!=null){
				switch (action) {
				case TRIP_DECLINE:
					updateNotification(true);					
					break;

				default:
					break;
				}
			}
			else{
				updateServiceMode();
				updateNotification(false);
				
				setUpGoogleApiClientIfNeeded();
		        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress)
		        {
		        	mInProgress = true;
		        	mGoogleApiClient.connect();
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
		    		        	updateNotification(false);
		    		            checkIgnitionHandler.postDelayed(this, GET_DEVICE_FREQUENCY);
		    	        	}
		    	        }
		    	    };
		    	    checkIgnitionHandler.postDelayed(checkIgnitionRunnable, 0);
		        }
		        
		        updateLocationUpdatesHandler();
			    
			    if(phoneScreenOnOffReceiver==null){
				    phoneScreenOnOffReceiver = new PhoneScreenOnOffReceiver();
				    IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
				    filter.addAction(Intent.ACTION_SCREEN_OFF);
				    filter.addAction(Intent.ACTION_USER_PRESENT);
				    registerReceiver(phoneScreenOnOffReceiver, filter);
			    }
			}
		}
		else
			stopSelf();
	    
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
	}
	
    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
	
	private void iBeaconStartMonitoring() {
        try {
        	System.out.println("ibeacon connect");
        	if(!beaconManager.isConnected()){
	            beaconManager.connect(new OnServiceBoundListener() {
	                @Override
	                public void onServiceBound() throws RemoteException {
	                	System.out.println("ibeacon start monitoring");
	                    beaconManager.startMonitoring();
	                }
	            });
        	}
        } catch (RemoteException e) {
//        	throw new IllegalStateException(e);
        	e.printStackTrace();
        }
    }
	
	private void updateServiceMode(){
		String mode = prefs.getString(Device.PREF_CURRENT_TRACKING_MODE, "");
		System.out.println("Mode: "+mode);
		
		if(!prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false))
			prefs.edit().putBoolean(Constants.PREF_TRIP_DECLINED, false).commit();
		
		if(prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_IBEACON)){
			if(beaconManager==null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){

		    	System.out.println("ubi ibeacon init");
				beaconManager = BeaconManager.newInstance(this);
		        beaconManager.setScanMode(BeaconManager.SCAN_MODE_BALANCED);
		        beaconManager.setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT);
		        beaconManager.setForceScanConfiguration(ForceScanConfiguration.DEFAULT);
		        beaconManager.registerMonitoringListener(new MonitoringListener() {
					
					@Override
					public void onRegionEntered(com.kontakt.sdk.android.device.Region arg0) {
		    			System.out.println("ibeacon region entered, bc: "+beaconConnected);
		    			iBeaconConnectedSaveEvent();					
					}
					
					@Override
					public void onRegionAbandoned(com.kontakt.sdk.android.device.Region arg0) {
		    			System.out.println("ibeacon region abandoned");
		    			if(beaconConnected){
			    			beaconConnected = false;
			    			savePoint(EVENT_BEACON_DISCONNECTED);
		    			}					
					}
					
					@Override
					public void onMonitorStop() {
		    			System.out.println("ibeacon monitor stop");
					}
					
					@Override
					public void onMonitorStart() {
		    			System.out.println("ibeacon monitor start");
					}
					
					@Override
					public void onBeaconsUpdated(com.kontakt.sdk.android.device.Region arg0, List<BeaconDevice> beacons) {
		    			System.out.println("ibeacons updated, "+beacons.size()+" beacon connected: "+beaconConnected+" trip in process: "+prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false));
		    			System.out.println("trip declined: "+prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false));
		    			iBeaconConnectedSaveEvent();					
					}
					
					@Override
					public void onBeaconAppeared(com.kontakt.sdk.android.device.Region arg0, BeaconDevice arg1) {
		    			System.out.println("ibeacon appeared");
						
					}
				});

		        final String beaconUniqueId = prefs.getString(Device.PREF_DEVICE_MEID, "");
		        
	        	System.out.println("ibeacon set filter : "+beaconUniqueId);
		        
		        beaconManager.addFilter(new CustomFilter() {
		            @Override
		            public Boolean apply(AdvertisingPackage object) {
		                //final UUID proximityUUID = object.getProximityUUID();
		                final double distance = object.getAccuracy();
		                final String uniuqeId = object.getBeaconUniqueId();

		                return uniuqeId.equals(beaconUniqueId) && distance <= ACCEPT_DISTANCE;
		            }
		        });
		    	iBeaconStartMonitoring();
			}
	    }
		else{
			if(beaconManager!=null){
				System.out.println("ibeacon monitoring stop on update");
				beaconManager.stopMonitoring();
			}
		}
		
		if(prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_OBDBLE)){
			if(jastecHandler==null){
				jastecHandler = new Handler();
	        }
			
			jastecTripStateListener = new OnSensorListener() {
				
				@Override
				public void onTripStop() {
					if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false))
						savePoint(EVENT_TRIP_STOP);
				}
				
				@Override
				public void onTripStart() {
					if(!prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
    	        		lastJastecPing = 0;
						savePoint(EVENT_TRIP_START);
					}
				}
				
				@Override
				public void onPing() {
					lastJastecPing = System.currentTimeMillis();
				}
			};
			
			jastecMan = JastecManager.getInstance(LocationService.this);
			jastecMan.setOnSensorListener(jastecTripStateListener);
			jastecMan.setContext(this);
	        
	        if(jastecRunnable==null){
	        	jastecHandler.removeCallbacksAndMessages(null);
	        	jastecRunnable = new Runnable() {
	    	        @Override
	    	        public void run() {
	    	        	System.out.println("Jastec connection handler");
	    	        	jastecMan.connect();
	    	        	jastecHandler.postDelayed(this, 5000);
	    	        	
	    	        	if(lastJastecPing!=0 && System.currentTimeMillis() - lastJastecPing >= 5000){
	    	        		if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
	    	        			//Bugsnag.notify(new RuntimeException("Trip stop, no Jastec ping for 5 seconds"));
	    						savePoint(EVENT_TRIP_STOP);
	    	        		}
	    	        	}
	    	        }
	    	    };
	    	    jastecHandler.postDelayed(jastecRunnable, 0);
	        }			
		}
		else{
			if(jastecHandler!=null){
				jastecHandler.removeCallbacksAndMessages(null);
				jastecHandler = null;
				jastecRunnable = null;
				jastecMan.disconnect();
			}
		}
		
		
		switch (mode) {
		case Device.MODE_LIGHT_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(0);
	        mLocationRequest.setInterval(5*60*1000);
	        mLocationRequest.setFastestInterval(30*1000);

			break;
		case Device.MODE_SIGNIFICATION_TRACKING:
			
			break;
		case Device.MODE_MEDIUM_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(0);
	        mLocationRequest.setInterval(40*1000);
	        mLocationRequest.setFastestInterval(20*1000);
			break;
		case Device.MODE_SUPER_TRACKING:
			mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationRequest.setSmallestDisplacement(0);
	        mLocationRequest.setInterval(1*1000);
	        mLocationRequest.setFastestInterval(1*1000);
			break;

		default:
			break;
		}
		
		if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			if(!mode.equals(Device.MODE_SIGNIFICATION_TRACKING))
				LocationServices.FusedLocationApi.requestLocationUpdates(
		                mGoogleApiClient,
		                mLocationRequest,
		                this);
		}
	}
	
	private void iBeaconConnectedSaveEvent(){
		boolean inTrip = prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false);
		
		if(!beaconConnected || !inTrip){
			beaconConnected = true;
			
			if(inTrip){
				savePoint(EVENT_BEACON_CONNECTED);
			}
			else{
				savePoint(EVENT_TRIP_START);
			}
		}		
	}
	
	private void showTripDeclineNotification(){

		Intent resultIntent = new Intent(this, TripDeclineActivity.class);
		
		showNotification(TRIP_DECLINE_NOTIFICATION_ID, "Trip Tracking Started! If you are not driving please tap here.", resultIntent, false);
		
    	Runnable tripDeclineNotificationRunnable = new Runnable() {
	        @Override
	        public void run() {
	        	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        	mNotificationManager.cancel(TRIP_DECLINE_NOTIFICATION_ID);
	        }
	    };
	    
    	tripDeclineNotificationHandler.removeCallbacksAndMessages(null);
    	tripDeclineNotificationHandler.postDelayed(tripDeclineNotificationRunnable, 10*1000);
	}
	
	private void updateNotification(boolean sonundAndVibration){
		
		String servicesToEnable = "";
		int servicesToEnableCount = 0;
		boolean inTrip = prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false) && !prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false);
		String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		
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
			if(deviceType.equals(Device.DEVICE_TYPE_IBEACON) && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
				showNotification(SERVICES_DISABLED_NOTIFICATION_ID,
		    		"Your phone does not support iBeacons",
		    		new Intent(this, InitActivity.class),
		    		!sonundAndVibration);
			}
			else
				showNotification(SERVICES_DISABLED_NOTIFICATION_ID,
	    			"Please, enable next service" + (servicesToEnableCount > 1 ? "s" : "") + " for better experience: "+servicesToEnable,
	    			new Intent(android.provider.Settings.ACTION_SETTINGS),
	    			!sonundAndVibration);
		}
		
		String notificationText = "Trip Tracking " + (inTrip ? "Started." : "Stopped.") /*+ " Mode: " + prefs.getString(Device.PREF_CURRENT_TRACKING_MODE, "none") + "."*/;
		
		if(inTrip){
			if(!prefs.getBoolean(Constants.PREF_CHARGER_CONNECTED, false) && !deviceType.equals(Device.DEVICE_TYPE_OBD)){
				notificationText += " Don't forget to plugin your phone. Your battery will thank you!";
			}
			notificationText += " Drive Safe!";
		}
		
		Intent resultIntent = new Intent(this, InitActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);//stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getResources().getString(R.string.app_name))
		        .setContentText(notificationText)
		        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
		        .setContentIntent(resultPendingIntent)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setOngoing(true);
		
		if(sonundAndVibration && !deviceType.equals(Device.DEVICE_TYPE_OBD)){
			mBuilder
	        .setVibrate(inTrip ? new long[]{0, 1000} : new long[]{0, 400, 200, 400})
	        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + (inTrip ? R.raw.beep_sound : R.raw.double_beep_sound)));
		}
		
		Notification n = mBuilder.build();
		
		startForeground(PERMANENT_NOTIFICATION_ID, n);
	}
	
	private void showNotification(int id, String message, Intent resultIntent, boolean soundAndVibration){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getString(R.string.app_name))
		        .setContentText(message)
		        .setPriority(NotificationCompat.PRIORITY_MAX)
		        .setAutoCancel(true);
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this,id,resultIntent,Intent.FLAG_ACTIVITY_NEW_TASK);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setStyle(new NotificationCompat.BigTextStyle()
        .bigText(message));

		Notification n = mBuilder.build();
		if(soundAndVibration){
			n.defaults |= Notification.DEFAULT_SOUND;
			n.defaults |= Notification.DEFAULT_VIBRATE;
		}
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, n);
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
	
	private void updateLocationUpdatesHandler(){
		final String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		
		if(!deviceType.equals(Device.DEVICE_TYPE_OBD)){
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
			else{
				if(checkLocationUpdatesHandler==null){
					checkLocationUpdatesHandler = new Handler();
		        }
				
	        	checkLocationUpdatesHandler.removeCallbacksAndMessages(null);
		        
		        if(checkLocationUpdatesRunnable==null){
					checkLocationUpdatesRunnable = new Runnable() {
		    	        @Override
		    	        public void run() {
		    	        	System.out.println("checkLocationUpdatesRunnable");
		    	        	if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false)){
			    	        	if(System.currentTimeMillis() - lastLocationUpdateTime > MAX_STAY_TIME ||
			    	        			(deviceType.equals(Device.DEVICE_TYPE_IBEACON) && !beaconConnected && System.currentTimeMillis() - lastBeaconDisconnectMillis > MAX_BEACON_NO_CONNECTION_STAY_TIME)){
			    	        		//Bugsnag.notify(new RuntimeException("Trip stop, no location for 2 minutes"));
			    	        		System.out.println("Trip stop, no location for 2 minutes");
			    	        		savePoint(EVENT_TRIP_STOP);
			    	        		checkLocationUpdatesHandler.removeCallbacks(checkLocationUpdatesRunnable);
			    	        	}
			    	        	else{
				    	        	System.out.println("checkLocationUpdatesRunnable reposted");
			    	        		checkLocationUpdatesHandler.postDelayed(this, checkLocationUpdatesFrequency);
			    	        		
			    					new SendEventsRequest(LocationService.this, false).execute();	
			    	        	}
		    	        	}
		    	        }
		    	    };
		        }
		        else{
	        		checkLocationUpdatesHandler.postDelayed(checkLocationUpdatesRunnable, checkLocationUpdatesFrequency);		        	
		        }
			}
		}
	}
	
	@Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		
        boolean deviceInTrip = prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false);

        if(deviceType.equals(Device.DEVICE_TYPE_OBD)){
        	Device.checkDevice(this);
        }
        else if(deviceInTrip){
        	
        	String event = "";
        	
        	if(deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) && deviceInTrip){
            	
            	if(location.getSpeed() < TRIP_STAY_SPEED){
            		if(stayFromMillis==0)
    	    			stayFromMillis = System.currentTimeMillis();
    	    		else if(System.currentTimeMillis() - stayFromMillis > MAX_STAY_TIME){
    	    			event = EVENT_TRIP_STOP;
    	    		}
            	}
            	else {
            		stayFromMillis = 0;
    			}
            }

        	savePoint(location, event);
        	Intent i = new Intent(LogActivity.ACTION_LOGS);
			i.putExtra(LogActivity.BROADCAST_INTENT_EXTRA_MESSAGE, "GPS Speed = " + location.getSpeed());
			sendBroadcast(i);
        }
        else{
        	if(deviceType.equals(Device.DEVICE_TYPE_SMARTPHONE) && location.getSpeed()>TRIP_START_SPEED){
            	savePoint(location, EVENT_TRIP_START);
            }
        }
        
        lastLocationUpdateTime = System.currentTimeMillis();
    }
	
	private String getRawData(){
		String deviceType = prefs.getString(Device.PREF_DEVICE_TYPE, "");
		if(deviceType.equals(Device.DEVICE_TYPE_OBDBLE) && jastecMan != null){
			return "speed: " + jastecMan.getLastSpeed() + ", rpm: " + jastecMan.getLastRPM();
		}
		else
			return "";
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
	        
//	        if(event.equals("")){
//	        	//Bugsnag.addToTab("User", "Point data", msg);
//	        }
//	        else{
//	        	Bugsnag.notify(new RuntimeException(msg));
//	        }
			
			Editor e = prefs.edit();
	        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+location.getLatitude());
	        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+location.getLongitude());
	        
			if(event.equals(EVENT_TRIP_START)){
				e.putBoolean(Device.PREF_IN_TRIP_NOW, true).commit();
	    		stayFromMillis = 0;
	    		lastLocationUpdateTime = System.currentTimeMillis();
	        	updateLocationUpdatesHandler();
	    		updateNotification(true); 
				//showTripDeclineNotification();	
			}
			else if(event.equals(EVENT_TRIP_STOP)){
				e.putBoolean(Device.PREF_IN_TRIP_NOW, false).commit();
	    		updateNotification(prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false) ? false : true);
				prefs.edit().putBoolean(Constants.PREF_TRIP_DECLINED, false).commit();
			}
			else
				e.commit();
	    	
			if(!prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false)){
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
		    			getRawData()), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
		    	dbhelper.close();
			}
			
			if(event.equals(EVENT_TRIP_START) || event.equals(EVENT_TRIP_STOP)){
				System.out.println("SEND - "+event);
				new SendEventsRequest(this, true).execute();	
			}
	    	
	    	if(!TextUtils.isEmpty(event))
	    		updateNotification(false);
        }
	   	
        Device.checkDevice(this);
	}

	private void savePoint(Location location){
		savePoint(location, "");
	}
	
	private void savePoint(String event, long eventTimeMillis){
		
		if(event.equals(EVENT_BEACON_DISCONNECTED)){
			lastBeaconDisconnectMillis = System.currentTimeMillis();
		}
		else if(event.equals(EVENT_BEACON_CONNECTED)){
			lastBeaconDisconnectMillis = 0;
		}
		
		String msg = "point event: "+event;
		System.out.println(msg);
		logger.info(msg);
		
		Editor e = prefs.edit();
		if(event.equals(EVENT_TRIP_START)){
			e.putBoolean(Device.PREF_IN_TRIP_NOW, true).commit();
			stayFromMillis = 0;
			lastLocationUpdateTime = System.currentTimeMillis();
    		updateNotification(true);
			//showTripDeclineNotification();
		}
		else if(event.equals(EVENT_TRIP_STOP)){
			e.putBoolean(Device.PREF_IN_TRIP_NOW, false).commit();
    		updateNotification(prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false) ? false : true);
			prefs.edit().putBoolean(Constants.PREF_TRIP_DECLINED, false).commit();
			
			if(lastBeaconDisconnectMillis!=0)
				eventTimeMillis = lastBeaconDisconnectMillis;
		}
		
		if(!prefs.getBoolean(Constants.PREF_TRIP_DECLINED, false)){
			DbHelper dbhelper = DbHelper.getInstance(this);
			dbhelper.saveTrackingEvent(new Tracking(
					eventTimeMillis, 
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
					getRawData()), prefs.getLong(Constants.PREF_DRIVER_ID, 0));
			dbhelper.close();
		}
		
		if(event.equals(EVENT_TRIP_START) || event.equals(EVENT_TRIP_STOP)){
			System.out.println("SEND - "+event);
			new SendEventsRequest(this, true).execute();
		}
		
//		Bugsnag.notify(new RuntimeException("event: "+event));
		updateLocationUpdatesHandler();
		updateNotification(false);
        Device.checkDevice(this);
        
	}
	
	private void savePoint(String event){
		savePoint(event, System.currentTimeMillis());
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
		
		if(jastecHandler!=null){
			jastecHandler.removeCallbacksAndMessages(null);
			jastecHandler = null;
			jastecRunnable = null;
		}
		if(jastecMan!=null)
			jastecMan.disconnect();
		
		mInProgress = false;
        if(servicesAvailable && mGoogleApiClient != null) {
        	if(mGoogleApiClient.isConnected())
        		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
	        // Destroy the current location client
        	mGoogleApiClient = null;
        }
        
        if(prefs.getBoolean(Device.PREF_IN_TRIP_NOW, false) && !prefs.getString(Device.PREF_DEVICE_TYPE, "").equals(Device.DEVICE_TYPE_OBD)){
            Bugsnag.notify(new RuntimeException("Trip stop, service destroy"));
        	System.out.println("Trip stop, service destroyed");
        	savePoint(EVENT_TRIP_STOP);
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
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
    	
		updateServiceMode();
    	
		if(mGoogleApiClient!=null){
	    	Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
	    	if(lastLocation!=null){
		    	Editor e = prefs.edit();
		        e.putString(Constants.PREF_MOBILE_LATITUDE, ""+lastLocation.getLatitude());
		        e.putString(Constants.PREF_MOBILE_LONGITUDE, ""+lastLocation.getLongitude());
		        e.commit();
	    	}
	        lastLocationUpdateTime = System.currentTimeMillis();
	        System.out.println("Location service Connected");
		}
		else{
			Bugsnag.notify(new RuntimeException("Location client null"));
		}
    }
 
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
//    @Override
//    public void onDisconnected() {
//        // Turn off the request flag
//        mInProgress = false;
//        // Destroy the current location client
//        mLocationClient = null;
//        // Display the connection status
//        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
//    }
 
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

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		mInProgress = false;
		mGoogleApiClient = null;
	}
}