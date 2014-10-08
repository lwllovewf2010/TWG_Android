package com.modusgo.ubi;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.modusgo.demo.R;
import com.modusgo.ubi.utils.Utils;
import com.viewpagerindicator.CirclePageIndicator;

public class SignInActivity extends FragmentActivity {
    
	View layoutProgress;
	View layoutFields;
	EditText editUsername;
	EditText editPassword;
	
	private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    
	SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
	    
	    if(!prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
	    	startActivity(new Intent(SignInActivity.this, HomeActivity.class));
			finish();
	    }
	    
	    layoutFields = findViewById(R.id.loginFields);
	    layoutProgress = findViewById(R.id.loginProgress);
	    
	    editUsername = (EditText)findViewById(R.id.username);
	    editPassword = (EditText)findViewById(R.id.password);

	    Button btnSignIn = (Button)findViewById(R.id.btnSignIn);
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressLogging);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    btnSignIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new LoginTask(SignInActivity.this).execute("login.json");
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
            	return ScreenSlidePageFragment.newInstance(R.drawable.slide_4, "Keep your car happy.", "Monitor your vehicle’s health, preventative\nmaintenance schedule, warrantly information,\nand even recalls!");
            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
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
		Animation fadeIn = AnimationUtils.loadAnimation(SignInActivity.this,android.R.anim.fade_in);
		fadeIn.setAnimationListener(new MyAnimationListener(v, false));
		return fadeIn;
	}
	
	private Animation getFadeOutAnmation(View v){
		Animation fadeOut = AnimationUtils.loadAnimation(SignInActivity.this,android.R.anim.fade_out);
		fadeOut.setAnimationListener(new MyAnimationListener(v, true));
		return fadeOut;
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
			fadeInProgress = getFadeInAnmation(layoutProgress);
			fadeOutProgress = getFadeOutAnmation(layoutProgress);
			fadeInFields = getFadeInAnmation(layoutFields);
			fadeOutFields = getFadeOutAnmation(layoutFields);
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
			
	        return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			Editor e = prefs.edit();
			e.putString(Constants.PREF_AUTH_KEY, responseJSON.getString("auth_key"));
			if(!responseJSON.isNull("driver"))
				e.putString(Constants.PREF_ROLE, responseJSON.getJSONObject("driver").optString("role"));
			if(!responseJSON.isNull("device")){
				JSONObject deviceJSON = responseJSON.getJSONObject("device");
				e.putString(Constants.PREF_DEVICE_MEID, deviceJSON.getString("meid"));
				e.putString(Constants.PREF_DEVICE_TYPE, deviceJSON.getString("type"));
				e.putString(Constants.PREF_DEVICE_DATA_URL, deviceJSON.getString("data_url"));
				e.putString(Constants.PREF_DEVICE_AUTH_KEY, deviceJSON.getString("auth_key"));
			}
			e.commit();
			
			if(!responseJSON.isNull("vehicles")){
				JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
				DriversHelper dHelper = DriversHelper.getInstance();
				
				dHelper.drivers.clear();
				for (int i = 0; i < vehiclesJSON.length(); i++) {
					JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
	
					Driver d = new Driver();
					d.id = vehicleJSON.getLong("id");
					
					if(vehicleJSON.isNull("driver")){
						JSONObject driverJSON = vehicleJSON.getJSONObject("driver");
						d.name = driverJSON.optString("name");
						d.imageUrl = driverJSON.optString("photo");
						d.markerIcon = driverJSON.optString("icon");
					}
					
					if(!vehicleJSON.isNull("car")){
						JSONObject carJSON = vehicleJSON.getJSONObject("car");
						d.carVIN = carJSON.optString("vin");
						d.carMake = carJSON.optString("make");
						d.carModel = carJSON.optString("model");
						d.carYear = carJSON.optString("year");
						d.carFuelLevel = carJSON.optInt("fuel_level", -1);
						d.carCheckup = carJSON.optBoolean("checkup");
					}
					
					if(!vehicleJSON.isNull("location")){
						JSONObject locationJSON = vehicleJSON.getJSONObject("location");
						d.latitude = locationJSON.optDouble("latitude");
						d.longitude = locationJSON.optDouble("longitude");
						d.address = locationJSON.optString("address");
						d.lastTripDate = Utils.fixTimezoneZ(locationJSON.optString("last_trip_time","Undefined"));
						d.lastTripId = locationJSON.optLong("last_trip_id");
					}
					
					if(!vehicleJSON.isNull("stats")){
						JSONObject statsJSON = vehicleJSON.getJSONObject("stats");
						d.score = statsJSON.optInt("score");
						d.grade = statsJSON.optString("grade");
						d.totalTripsCount = statsJSON.optInt("trips");
						d.totalDrivingTime = statsJSON.optInt("time");
						d.totalDistance = statsJSON.optDouble("distance");
						d.totalBraking = statsJSON.optInt("braking");
						d.totalAcceleration = statsJSON.optInt("acceleration");
						d.totalSpeeding = statsJSON.optInt("speeding");
						d.totalSpeedingDistance = statsJSON.optDouble("speeding_distance");
						d.alerts = statsJSON.optInt("new_alerts");
					}
					
					dHelper.drivers.add(d);
				}
			}
			
			startActivity(new Intent(SignInActivity.this, HomeActivity.class));
			finish();
			super.onSuccess(responseJSON);
		}
		
		@Override
		protected void onError(String message) {
			layoutProgress.startAnimation(fadeOutProgress);
			layoutFields.startAnimation(fadeInFields);
			super.onError(message);
		}
		
		@Override
		protected void onError401() {
			//disable
			onError("Invalid login or password");
		}
	}
}
