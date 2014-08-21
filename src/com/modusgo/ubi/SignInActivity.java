package com.modusgo.ubi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.modusgo.ubi.utils.RequestPost;
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
				new LoginTask().execute();
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
	
	class LoginTask extends AsyncTask<Void, Void, HttpResponse>{

		Animation fadeIn;
		Animation fadeOut;
		
		public LoginTask() {			
			fadeOut = AnimationUtils.loadAnimation(SignInActivity.this,android.R.anim.fade_out);
			fadeOut.setFillAfter(true);
			
			fadeIn = AnimationUtils.loadAnimation(SignInActivity.this,android.R.anim.fade_in);
			fadeIn.setFillAfter(true);
		}
		
		@Override
		protected void onPreExecute() {
			layoutProgress.setVisibility(View.VISIBLE);
			layoutProgress.startAnimation(fadeIn);
			layoutFields.startAnimation(fadeOut);
			super.onPreExecute();
		}
		
		@Override
		protected HttpResponse doInBackground(Void... params) {
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", editUsername.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("password", editPassword.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("platform", Constants.API_PLATFORM));
	        nameValuePairs.add(new BasicNameValuePair("mobile_id", Utils.getUUID(SignInActivity.this)));
			
			return new RequestPost(Constants.API_BASE_URL+"login.json", nameValuePairs).execute();
			
		}
		
		@Override
		protected void onPostExecute(HttpResponse result) {
			if(result.getStatusLine().getStatusCode()>=200 && result.getStatusLine().getStatusCode()<300){
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(result.getEntity().getContent(), "UTF-8"));
					String json = reader.readLine();
					JSONTokener tokener = new JSONTokener(json);
					JSONObject finalResult = new JSONObject(tokener);
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
					prefs.edit().putString(Constants.PREF_AUTH_KEY, finalResult.getString("auth_key")).commit();
					
					System.out.println(finalResult);
				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}
				
				
				startActivity(new Intent(SignInActivity.this, HomeActivity.class));
				finish();
			}
			else{
				layoutProgress.startAnimation(fadeOut);
				layoutFields.startAnimation(fadeIn);
				Toast.makeText(SignInActivity.this, "Error "+result.getStatusLine().getStatusCode()+": "+result.getStatusLine().getReasonPhrase(), Toast.LENGTH_SHORT).show();
			}
			
			super.onPostExecute(result);
		}
	}
}
