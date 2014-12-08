package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.modusgo.dd.CallSaverService;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.requesttasks.SendEventsRequest;
import com.modusgo.ubi.utils.RequestGet;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class InitActivity extends FragmentActivity {
    
	View layoutProgress;
	View layoutFields;
	EditText editClientId;
	TextView tvError;
	TextView tvProgress;
	
	Animation fadeInProgress;
	Animation fadeOutProgress;
	Animation fadeInFields;
	Animation fadeOutFields;
	
	SharedPreferences prefs;
	
	String clientId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
	    getActionBar().hide();
	    
	    if(!ImageLoader.getInstance().isInited()){
	        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
			ImageLoader.getInstance().init(config);
	    }
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(InitActivity.this);
	    clientId = prefs.getString(Constants.PREF_CLIENT_ID, "");
	    
	    ImageView imageBg = (ImageView) findViewById(R.id.imageBg);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
    	
    	ImageLoader.getInstance().displayImage(prefs.getString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, ""), imageBg, options);
	    
	    layoutFields = findViewById(R.id.llFields);
	    layoutProgress = findViewById(R.id.rlProgress);
	    tvError = (TextView) findViewById(R.id.tvError);
	    tvProgress = (TextView) findViewById(R.id.tvProgress);
	    editClientId = (EditText)findViewById(R.id.editClientId);
	    
	    fadeInProgress = getFadeInAnmation(layoutProgress);
		fadeOutProgress = getFadeOutAnmation(layoutProgress);
		fadeInFields = getFadeInAnmation(layoutFields);
		fadeOutFields = getFadeOutAnmation(layoutFields);
	    
	    editClientId.setText(clientId);
	    

    	Intent i = new Intent(this, CallSaverService.class);
    	i.putExtra("action", TelephonyManager.EXTRA_STATE_IDLE);
    	startService(i);
	    
	    
	    if(!clientId.equals("")){
		    layoutFields.setVisibility(View.GONE);
	    	new ServerCheckTask().execute();
	    }
	    
	    Button btnSubmit = (Button)findViewById(R.id.btnSubmit);
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ServerCheckTask().execute();
			}
		});
	}
	
	public class MyAnimationListener implements AnimationListener {
	    
		View view;
	    boolean hideView;
	    
	    public MyAnimationListener(View v, boolean hideView) {
			view = v;
			this.hideView = hideView;
		}
	    
	    public void onAnimationEnd(Animation animation) {
	    	if(hideView)
	    		view.setVisibility(View.GONE);
	    	else
	    		view.setVisibility(View.VISIBLE);
	    }
	    
	    public void onAnimationRepeat(Animation animation) {
	    }
	    public void onAnimationStart(Animation animation) {
	    }
	}
	
	private Animation getFadeInAnmation(View v){
		Animation fadeIn = AnimationUtils.loadAnimation(InitActivity.this,android.R.anim.fade_in);
		fadeIn.setAnimationListener(new MyAnimationListener(v, false));
		return fadeIn;
	}
	
	private Animation getFadeOutAnmation(View v){
		Animation fadeOut = AnimationUtils.loadAnimation(InitActivity.this,android.R.anim.fade_out);
		fadeOut.setAnimationListener(new MyAnimationListener(v, true));
		return fadeOut;
	}
	
	class ServerCheckTask extends AsyncTask<Void, Void, Boolean>{

		JSONArray welcomeScreens = null;
		int status;
		String message = "Client ID not found or server is unavailable";
		
		protected List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
		
		@Override
		protected void onPreExecute() {
			if(layoutProgress.getVisibility()==View.GONE){
				layoutProgress.setVisibility(View.VISIBLE);
				layoutProgress.startAnimation(fadeInProgress);
				layoutFields.startAnimation(fadeOutFields);
			}
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			String clientId = editClientId.getText().toString();

	        requestParams.add(new BasicNameValuePair("auth_key", prefs.getString(Constants.PREF_AUTH_KEY, "")));
	        System.out.println(requestParams);
			
			HttpResponse result = new RequestGet(Constants.API_BASE_URL_PREFIX+clientId+Constants.API_BASE_URL_POSTFIX+"info.json", requestParams).execute();
			if(result==null){
				status = 0;
				//message = "Connection error";
				
				try {
					synchronized (this) {
						wait(1000);	
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				return false;
			}
			
	        try {
	        	status = result.getStatusLine().getStatusCode();
	        	
	        	if(status>=200 && status<300){
					JSONObject responseJSON = Utils.getJSONObjectFromHttpResponse(result);
					if(responseJSON.getString("status").equals("success")){
						Editor e = prefs.edit();
						e.putString(Constants.PREF_CLIENT_ID, clientId);
						
						if(responseJSON.optString("auth_key").equals("")){
							e.putString(Constants.PREF_AUTH_KEY, "");
						}	
						
						if(responseJSON.has("driver")){
							JSONObject driverJSON = responseJSON.getJSONObject("driver");
							e.putString(Constants.PREF_ROLE, driverJSON.optString(Constants.PREF_ROLE));
							e.putLong(Constants.PREF_DRIVER_ID, driverJSON.optLong(Constants.PREF_DRIVER_ID));
							e.putLong(Constants.PREF_VEHICLE_ID, driverJSON.optLong(Constants.PREF_VEHICLE_ID));
							e.putString(Constants.PREF_FIRST_NAME, driverJSON.optString(Constants.PREF_FIRST_NAME));
							e.putString(Constants.PREF_LAST_NAME, driverJSON.optString(Constants.PREF_LAST_NAME));
							e.putString(Constants.PREF_EMAIL, driverJSON.optString(Constants.PREF_EMAIL));
							e.putString(Constants.PREF_ROLE, driverJSON.optString(Constants.PREF_ROLE));
							e.putString(Constants.PREF_PHONE, driverJSON.optString(Constants.PREF_PHONE));
							e.putString(Constants.PREF_TIMEZONE, driverJSON.optString(Constants.PREF_TIMEZONE));
							e.putString(Constants.PREF_TIMEZONE_OFFSET, driverJSON.optString(Constants.PREF_TIMEZONE_OFFSET));
							e.putString(Constants.PREF_PHOTO, driverJSON.optString(Constants.PREF_PHOTO));
						}
						
						if(responseJSON.has("info")){
							JSONObject infoJSON = responseJSON.getJSONObject("info");
							e.putBoolean(Constants.PREF_DIAGNOSTIC, infoJSON.optBoolean("diagnostic"));
							e.putString(Constants.PREF_UNITS_OF_MEASURE, infoJSON.optString("unit_of_measure","mile"));
							e.putString(Constants.PREF_CONTACT_PHONE, infoJSON.optString("contact_phone"));
							e.putString(Constants.PREF_AGENT_PHONE, infoJSON.optString("agent_phone"));
							e.putBoolean(Constants.PREF_FIND_MECHANIC_ENABLED, infoJSON.optBoolean("find_mechanic_enabled"));
							e.putBoolean(Constants.PREF_DTC_PRICES_ENABLED, infoJSON.optBoolean("dtc_prices_enabled"));
							e.putBoolean(Constants.PREF_MAINTENANCE_PRICES_ENABLED, infoJSON.optBoolean("maintenance_prices"));
							
							String trackingId = infoJSON.optString("");
							if(trackingId.equals("") || trackingId.equals("false")){
								GoogleAnalytics.getInstance(InitActivity.this).setAppOptOut(true);
							}
							else{
								GoogleAnalytics.getInstance(InitActivity.this).setAppOptOut(false);
								e.putString(Constants.PREF_GA_TRACKING_ID, trackingId);
							}
							
							if(infoJSON.has("welcome"))
								welcomeScreens = infoJSON.getJSONArray("welcome");
							
							if(infoJSON.has("branding")){
								JSONObject brandingJSON = infoJSON.getJSONObject("branding");
								e.putString(Constants.PREF_BR_LOGIN_SCREEN_BG_IMAGE, brandingJSON.optString("login_screen_bg_image"));
								e.putString(Constants.PREF_BR_LOGIN_SCREEN_LOGO, brandingJSON.optString("login_screen_logo"));
								e.putString(Constants.PREF_BR_BUTTONS_BG_COLOR, brandingJSON.optString("buttons_bg_color", Constants.BUTTON_BG_COLOR));
								e.putString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, brandingJSON.optString("buttons_text_color", Constants.BUTTON_TEXT_COLOR));
								e.putString(Constants.PREF_BR_TITLE_BAR_BG, brandingJSON.optString("title_bar_bg"));
								e.putString(Constants.PREF_BR_TITLE_BAR_BG_COLOR, brandingJSON.optString("title_bar_bg_color", Constants.TITLE_BAR_BG_COLOR));
								e.putString(Constants.PREF_BR_TITLE_BAR_TEXT_COLOR, brandingJSON.optString("title_bar_text_color", Constants.TITLE_BAR_TEXT_COLOR));
								e.putString(Constants.PREF_BR_MENU_LOGO, brandingJSON.optString("menu_logo"));
								e.putString(Constants.PREF_BR_SWITCH_DRIVER_MENU_BUTTON_COLOR, brandingJSON.optString("switch_driver_menu_button_bg_color", Constants.SWITCH_DRIVER_BUTTON_BG_COLOR));
								e.putString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, brandingJSON.optString("list_header_line_color", Constants.LIST_HEADER_LINE_COLOR));
							}
						}
						
						if(responseJSON.has("vehicles")){
							JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
							ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
							for (int i = 0; i < vehiclesJSON.length(); i++) {
								JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
								vehicles.add(Vehicle.fromJSON(getApplicationContext(), vehicleJSON));
							}
							
							DbHelper dbHelper = DbHelper.getInstance(InitActivity.this);
							dbHelper.saveVehicles(vehicles);
							dbHelper.close();
						}
						
						if(responseJSON.has("device")){
							JSONObject deviceJSON = responseJSON.getJSONObject("device");
							e.putString(Constants.PREF_DEVICE_MEID, deviceJSON.optString("meid"));
							e.putString(Constants.PREF_DEVICE_TYPE, deviceJSON.optString("type"));
							e.putString(Constants.PREF_DEVICE_DATA_URL, deviceJSON.optString("data_url"));
							e.putString(Constants.PREF_DEVICE_AUTH_KEY, deviceJSON.optString("auth_key"));
							e.putBoolean(Constants.PREF_DEVICE_EVENTS, deviceJSON.optBoolean("events"));
							e.putBoolean(Constants.PREF_DEVICE_TRIPS, deviceJSON.optBoolean("trips"));
						}

						e.commit();
						return true;
					}
	        	}
	        	else{
	        		//message = "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase();
			        return false;
	        	}
			} catch (Exception e) {
				status = 0;
				//message = "Wrong Client ID";
				e.printStackTrace();
			}
	        
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
			    new SendEventsRequest(InitActivity.this).execute("");
			    
				if(welcomeScreens!=null && welcomeScreens.length()>0){					
					Intent i = new Intent(InitActivity.this, WelcomeActivity.class);
					i.putExtra(WelcomeActivity.SAVED_SCREENS, welcomeScreens.toString());
					startActivity(i);
				}
				else
					startActivity(new Intent(InitActivity.this, SignInActivity.class));
				overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
				finish();
			}
			else{
				if(!clientId.equals("")){
					tvProgress.setText("Check your internet connection...");
					new ServerCheckTask().execute();
				}
				else{
					layoutFields.setVisibility(View.VISIBLE);
					layoutProgress.startAnimation(fadeOutProgress);
					layoutFields.startAnimation(fadeInFields);
					tvError.setText(message);
				}
			}
			
			super.onPostExecute(result);
		}
	}
}
