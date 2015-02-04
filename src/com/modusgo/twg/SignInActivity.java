package com.modusgo.twg;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.requesttasks.BasePostRequestAsyncTask;
import com.modusgo.twg.utils.Device;
import com.modusgo.twg.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.viewpagerindicator.CirclePageIndicator;

public class SignInActivity extends FragmentActivity {

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	View layoutProgress;
	View layoutFields;
	EditText editUsername;
	EditText editPassword;
	
	private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    
	SharedPreferences prefs;

    Context context;
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regid;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
	    context = getApplicationContext();
	    
	    if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
	    	startActivity(new Intent(SignInActivity.this, HomeActivity.class));
			finish();
	    }
	    
	    ImageView imageBg = (ImageView) findViewById(R.id.imageBg);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
        System.out.println("BG : "+prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, ""));
    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, ""), imageBg, options);
    	
    	ImageView imageLogo = (ImageView) findViewById(R.id.imageLogo);
    	DisplayImageOptions optionsLogo = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_LOGO, ""), imageLogo, optionsLogo);
	    
	    layoutFields = findViewById(R.id.loginFields);
	    layoutProgress = findViewById(R.id.loginProgress);
	    
	    editUsername = (EditText)findViewById(R.id.username);
	    editPassword = (EditText)findViewById(R.id.password);

	    Button btnSignIn = (Button)findViewById(R.id.btnSignIn);
	    btnSignIn.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
	    try{
	    	btnSignIn.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    Button btnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);
	    final String clientId = prefs.getString(Constants.PREF_CLIENT_ID, "");
		if(!TextUtils.isEmpty(clientId)){
			btnForgotPassword.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = "http://" + clientId + ".test.modusgo.com/drivers/password/new";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				}
			});
		}
		else{
			btnForgotPassword.setVisibility(View.INVISIBLE);
		}
		
	    
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSignIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSignIn();
			}
		});
	    
	    mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        
        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.pagerCircles);
        indicator.setViewPager(mPager);
        //indicator.setSnap(true);
        indicator.setCentered(true);
        
        final float density = getResources().getDisplayMetrics().density;
        indicator.setDistanceBetweenCircles(14 * density);
        indicator.setRadius(6 * density);
        indicator.setPageColor(0xBFFFFFFF);//unactive circle color
        indicator.setFillColor(0xFF00aded);
        indicator.setStrokeWidth(0);

	}
	
	@Override
	protected void onResume() {
		 Utils.gaTrackScreen(this, "Sign In Sceen");
		super.onResume();
	}
	
	private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                return ScreenSlidePageFragment.newInstance(R.drawable.slide_1, "Get the best discount.", "View your driver behavior and become\na better driver.");
            case 1:
            	return ScreenSlidePageFragment.newInstance(R.drawable.slide_2, "Forgot where you parked?", "Instantly get walking directions to your car.");
            case 2:
            	return ScreenSlidePageFragment.newInstance(R.drawable.slide_3, "Explore your trips.", "Discover driving events and how they\nimpact your discount.");
            case 3:
            	return ScreenSlidePageFragment.newInstance(R.drawable.slide_4, "Keep your car happy.", "Monitor your vehicle's health, preventative\nmaintenance schedule, warrantly information,\nand even recalls!");
            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
	
	public void startSignIn(){
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.equals("")) {
                registerInBackground();
            }
            else{
            	afterGCMRegistration();
            }
        } else {
        	afterGCMRegistration();
            //registerError("No valid Google Play Services APK found.");
        }
	}
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        String registrationId = prefs.getString(Constants.PREF_GCM_REG_ID, "");
        if (registrationId.equals("")) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
        	System.out.println("context null? "+(context==null));
        	System.out.println("pack man null? "+(context.getPackageManager()==null));
        	
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
        	
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Constants.GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // Persist the regID - no need to register again.
                    storeRegistrationId(regid);
                } catch (Exception ex) {
                    msg = "Error: " + ex.getMessage();
                    ex.printStackTrace();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	System.out.println("gcm message: "+msg);
            	afterGCMRegistration();
            }
        }.execute(null, null, null);
    }
    
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREF_GCM_REG_ID, regId);
        editor.putInt(Constants.PREF_APP_VERSION, appVersion);
        editor.commit();
    }
    
    private void afterGCMRegistration(){
    	new LoginTask(SignInActivity.this).execute("login.json");
    }
	
	public static class ScreenSlidePageFragment extends Fragment {
        int mNum;
        int imageResource;
        String title;
        String description;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static ScreenSlidePageFragment newInstance(int imageResource, String title, String description) {
        	ScreenSlidePageFragment f = new ScreenSlidePageFragment();

            Bundle args = new Bundle();
            args.putInt("image", imageResource);
            args.putString("title", title);
            args.putString("description", description);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            imageResource = getArguments() != null ? getArguments().getInt("image") : -1;
            title = getArguments() != null ? getArguments().getString("title") : "";
            description = getArguments() != null ? getArguments().getString("description") : "";
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_slide, container, false);
            if(imageResource>=0){
            	ImageView image = (ImageView) v.findViewById(R.id.image);
            	image.setImageResource(imageResource);
            }
            TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvTitle.setText(title);
            TextView tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvDescription.setText(description);
            
            return v;
        }
    }
	
	class LoginTask extends BasePostRequestAsyncTask{
		
		Animation fadeInProgress;
		Animation fadeOutProgress;
		Animation fadeInFields;
		Animation fadeOutFields;
		int status;
		String message = "";
		
		
		public LoginTask(Context context) {
			super(context);
			fadeInProgress = com.modusgo.twg.utils.AnimationUtils.getFadeInAnmation(SignInActivity.this, layoutProgress);
			fadeOutProgress = com.modusgo.twg.utils.AnimationUtils.getFadeOutAnmation(SignInActivity.this, layoutProgress);
			fadeInFields = com.modusgo.twg.utils.AnimationUtils.getFadeInAnmation(SignInActivity.this, layoutFields);
			fadeOutFields = com.modusgo.twg.utils.AnimationUtils.getFadeOutAnmation(SignInActivity.this, layoutFields);
		}
		
		@Override
		protected void onPreExecute() {
			layoutProgress.setVisibility(View.VISIBLE);
			layoutProgress.startAnimation(fadeInProgress);
			layoutFields.startAnimation(fadeOutFields);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("email", editUsername.getText().toString()));
	        requestParams.add(new BasicNameValuePair("password", editPassword.getText().toString()));
	        requestParams.add(new BasicNameValuePair("platform", Constants.API_PLATFORM));
	        requestParams.add(new BasicNameValuePair("mobile_id", Utils.getUUID(SignInActivity.this)));
	        requestParams.add(new BasicNameValuePair("push_id", prefs.getString(Constants.PREF_GCM_REG_ID, "")));
	        System.out.println(requestParams);
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			Editor e = prefs.edit();
			DbHelper dbHelper = DbHelper.getInstance(SignInActivity.this);

			e.putString(Constants.PREF_AUTH_KEY, responseJSON.getString("auth_key"));
			if(responseJSON.has("driver")){
				JSONObject driverJSON = responseJSON.getJSONObject("driver");
				e.putLong(Constants.PREF_DRIVER_ID, driverJSON.optLong(Constants.PREF_DRIVER_ID));
				e.putLong(Constants.PREF_VEHICLE_ID, driverJSON.optLong(Constants.PREF_VEHICLE_ID));
				e.putString(Constants.PREF_FIRST_NAME, driverJSON.optString(Constants.PREF_FIRST_NAME));
				e.putString(Constants.PREF_LAST_NAME, driverJSON.optString(Constants.PREF_LAST_NAME));
				e.putString(Constants.PREF_EMAIL, driverJSON.optString(Constants.PREF_EMAIL));
				e.putString(Constants.PREF_ROLE, driverJSON.optString(Constants.PREF_ROLE));
				e.putString(Constants.PREF_PHONE, driverJSON.optString(Constants.PREF_PHONE));
				e.putString(Constants.PREF_TIMEZONE, driverJSON.optString(Constants.PREF_TIMEZONE));
				e.putString(Constants.PREF_PHOTO, driverJSON.optString(Constants.PREF_PHOTO));
			}
			if(responseJSON.has("device")){
				JSONObject deviceJSON = responseJSON.getJSONObject("device");
				e.putString(Device.PREF_DEVICE_TYPE, deviceJSON.optString("type"));
				e.putString(Device.PREF_DEVICE_MEID, deviceJSON.optString("meid"));
				e.putBoolean(Device.PREF_DEVICE_EVENTS, deviceJSON.optBoolean("events"));
				e.putBoolean(Device.PREF_DEVICE_IN_TRIP, !TextUtils.isEmpty(deviceJSON.optString("in_trip")));
				e.putString(Device.PREF_DEVICE_LATITUDE, deviceJSON.optString("latitude"));
				e.putString(Device.PREF_DEVICE_LONGITUDE, deviceJSON.optString("longitude"));
				e.putString(Device.PREF_DEVICE_LOCATION_DATE, deviceJSON.optString("location_date"));
			}
			e.commit();
			
			if(responseJSON.has("vehicles")){
				JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
				ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
				for (int i = 0; i < vehiclesJSON.length(); i++) {
					JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
					vehicles.add(Vehicle.fromJSON(getApplicationContext(), vehicleJSON));
				}
				
				dbHelper.saveVehicles(vehicles);
				dbHelper.close();
			}
			
//			//Before we go, calculate the distance traveled
	    	double oldTotalDistance = 0;
	    	double deltaDistance = 0;
	    	long vehicleId = prefs.getLong(Constants.PREF_VEHICLE_ID, 0);
		    prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
		    if(prefs.contains(Constants.PREF_TOTAL_DISTANCE))
		    {
		    	oldTotalDistance = prefs.getLong(Constants.PREF_TOTAL_DISTANCE, 0);
		    }
		    Vehicle vehicle = dbHelper.getVehicle(vehicleId);
		    deltaDistance =  vehicle.totalDistance - oldTotalDistance;
		    e.putLong(Constants.PREF_TOTAL_DISTANCE, (long) vehicle.totalDistance);
		    e.putLong(Constants.PREF_DELTA_DISTANCE, (long) deltaDistance);
			
			startActivity(new Intent(SignInActivity.this, HomeActivity.class));
			finish();
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			startActivity(new Intent(SignInActivity.this, HomeActivity.class));
			finish();
			//super.onError(message);
		}
		
		@Override
		protected void onError401() {
			layoutProgress.startAnimation(fadeOutProgress);
			layoutFields.startAnimation(fadeInFields);
			super.onError("Invalid login or password");
		}
	}
}
