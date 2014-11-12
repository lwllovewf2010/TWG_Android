package com.modusgo.ubi;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
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
	    
	    ImageView imageBg = (ImageView) findViewById(R.id.imageBg);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(R.drawable.login_bg)
        .showImageForEmptyUri(R.drawable.login_bg)
        .showImageOnFail(R.drawable.login_bg)
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
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
	    btnSignIn.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, "#f15b2a")));
	    try{
	    	btnSignIn.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, "#edf1f9")));
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    
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
	
	class LoginTask extends BasePostRequestAsyncTask{
		
		Animation fadeInProgress;
		Animation fadeOutProgress;
		Animation fadeInFields;
		Animation fadeOutFields;
		int status;
		String message = "";
		
		
		public LoginTask(Context context) {
			super(context);
			fadeInProgress = com.modusgo.ubi.utils.AnimationUtils.getFadeInAnmation(SignInActivity.this, layoutProgress);
			fadeOutProgress = com.modusgo.ubi.utils.AnimationUtils.getFadeOutAnmation(SignInActivity.this, layoutProgress);
			fadeInFields = com.modusgo.ubi.utils.AnimationUtils.getFadeInAnmation(SignInActivity.this, layoutFields);
			fadeOutFields = com.modusgo.ubi.utils.AnimationUtils.getFadeOutAnmation(SignInActivity.this, layoutFields);
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
				e.putString(Constants.PREF_DEVICE_MEID, deviceJSON.optString("meid"));
				e.putString(Constants.PREF_DEVICE_TYPE, deviceJSON.optString("type"));
				e.putString(Constants.PREF_DEVICE_DATA_URL, deviceJSON.optString("data_url"));
				e.putString(Constants.PREF_DEVICE_AUTH_KEY, deviceJSON.optString("auth_key"));
				e.putBoolean(Constants.PREF_DEVICE_EVENTS, deviceJSON.optBoolean("events"));
				e.putBoolean(Constants.PREF_DEVICE_TRIPS, deviceJSON.optBoolean("trips"));
			}
			e.commit();
			
			if(!responseJSON.isNull("vehicles")){
				JSONArray vehiclesJSON = responseJSON.getJSONArray("vehicles");
				ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
				for (int i = 0; i < vehiclesJSON.length(); i++) {
					JSONObject vehicleJSON = vehiclesJSON.getJSONObject(i);
					vehicles.add(Vehicle.fromJSON(getApplicationContext(), vehicleJSON));
				}
				
				DbHelper dbHelper = DbHelper.getInstance(SignInActivity.this);
				dbHelper.saveVehicles(vehicles);
				dbHelper.close();
			}
			
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
