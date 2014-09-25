package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ScoreFragment extends Fragment{
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
	View llProgress;
	View llContent;
	TextView tvScore;
	
	String[] additionalData;
	int[] percentageData;
	float[][] pieChartsData;
	Bundle[] circlesData;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getArguments()!=null){
			driverIndex = getArguments().getInt("id");
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(driver.imageUrl == null || driver.imageUrl.equals(""))
	    	imagePhoto.setImageResource(driver.imageId);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.imageUrl, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		llProgress = rootView.findViewById(R.id.llProgress);
		llContent = rootView.findViewById(R.id.llContent);
		tvScore = (TextView)rootView.findViewById(R.id.tvScore);
		
		((View)tvScore.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), EducationActivity.class);
				i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, R.string.your_score);
				startActivity(i);
			}
		});
		
        new GetScoreTask(getActivity()).execute("drivers/"+driver.id+"/score.json");
        
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		super.onSaveInstanceState(outState);
	}
	
	class GetScoreTask extends BaseRequestAsyncTask{

		public GetScoreTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llContent.setVisibility(View.GONE);
			llProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			llContent.setVisibility(View.VISIBLE);
			llProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
		
		@Override
		protected void onSuccess(JSONObject json) throws JSONException {
			System.out.println(json);
			
			tvScore.setText(json.getString("grade"));
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
			DecimalFormat df = new DecimalFormat("0.000");
			
			additionalData = new String[]{
					Utils.convertTime(driver.lastTripDate, sdf), 
					"1GXEK4538960L23", 
					Utils.convertTime(json.getString("created"), sdf), 
					Utils.convertTime(json.getString("startdate"), sdf), 
					df.format(json.getDouble("summary_distance"))+" Miles", 
					df.format(json.getDouble("summary_ead"))+" Miles"};
			
			
			percentageData = new int[]{
					json.getInt("score_pace"),
					json.getInt("score_anticipation"),
					json.getInt("score_aggression"),
					json.getInt("score_smoothness"),
					json.getInt("score_completeness"),
					json.getInt("score_consistency")
			};
			
			pieChartsData = new float[][]{
					new float[]{
							(float)json.getDouble("roadsettings_rural"),
							(float)json.getDouble("roadsettings_suburban"),
							(float)json.getDouble("roadsettings_urban")
					},new float[]{
							(float)json.getDouble("roadtype_major"),
							(float)json.getDouble("roadtype_local"),
							(float)json.getDouble("roadtype_trunk"),
							(float)json.getDouble("roadtype_minor")
					},new float[]{
							(float)json.getDouble("timeofday0"),
							(float)json.getDouble("timeofday1"),
							(float)json.getDouble("timeofday2"),
							(float)json.getDouble("timeofday3"),
							(float)json.getDouble("timeofday4"),
							(float)json.getDouble("timeofday5")
					}};
			
			JSONObject jsonMarks = json.getJSONObject("road_env_analysis");
			
			JSONObject jsonStats = json.getJSONObject("road_env_stats");
			
			circlesData = new Bundle[]{
					getPageBundle("suburban", jsonMarks, jsonStats),
					getPageBundle("urban", jsonMarks, jsonStats),
					getPageBundle("rural", jsonMarks, jsonStats)
			};
			
			super.onSuccess(json);
		}
		
		private Bundle getPageBundle(String pageName, JSONObject jsonMarks, JSONObject jsonStats) throws JSONException{
			JSONObject jsonMarkPage = jsonMarks.getJSONObject(pageName);
			
			JSONObject jsonStatsPage = jsonStats.getJSONObject(pageName);
			
			Bundle pageBundle = new Bundle();
			pageBundle.putBundle(CirclesFragment.SAVED_USE_OF_SPEED, getBundleStats("pace", jsonMarkPage, jsonStatsPage));
			pageBundle.putBundle(CirclesFragment.SAVED_CORNERING, getBundleStats("cornering", jsonMarkPage, jsonStatsPage));
			pageBundle.putBundle(CirclesFragment.SAVED_INTERSECTION_ACCEL, getBundleStats("junctionacceleration", jsonMarkPage, jsonStatsPage));
			pageBundle.putBundle(CirclesFragment.SAVED_ROAD_ACCEL, getBundleStats("roadacceleration", jsonMarkPage, jsonStatsPage));
			pageBundle.putBundle(CirclesFragment.SAVED_INTERSECTION_BRAKING, getBundleStats("junctionbrake", jsonMarkPage, jsonStatsPage));
			pageBundle.putBundle(CirclesFragment.SAVED_ROAD_BRAKING, getBundleStats("roadbrake", jsonMarkPage, jsonStatsPage));
			
			return pageBundle;
		}
		
		private Bundle getBundleStats(String statName, JSONObject jsonMarks, JSONObject jsonStats) throws JSONException{
			Bundle b = new Bundle();
			b.putIntArray("marks", getMarksFromJson(statName, jsonMarks));
			b.putDoubleArray("distances", getDistancesFromJson(statName, jsonStats));
			return b;
		}
		
		private int[] getMarksFromJson(String statName, JSONObject json) throws JSONException{

			JSONObject jsonUrbanHighway = json.getJSONObject("trunk");
			JSONObject jsonUrbanMajor = json.getJSONObject("major");
			JSONObject jsonUrbanMinor = json.getJSONObject("minor");
			JSONObject jsonUrbanLocal = json.getJSONObject("local");
			
			return new int[]{
					getIntOnStringScore(jsonUrbanHighway.getString(statName)),
					getIntOnStringScore(jsonUrbanMajor.getString(statName)),
					getIntOnStringScore(jsonUrbanMinor.getString(statName)),
					getIntOnStringScore(jsonUrbanLocal.getString(statName)),
					};
		}
		
		private double[] getDistancesFromJson(String statName, JSONObject json) throws JSONException{

			JSONObject jsonUrbanHighway = json.getJSONObject("trunk");
			JSONObject jsonUrbanMajor = json.getJSONObject("major");
			JSONObject jsonUrbanMinor = json.getJSONObject("minor");
			JSONObject jsonUrbanLocal = json.getJSONObject("local");
			
			return new double[]{
					jsonUrbanHighway.getDouble(statName),
					jsonUrbanMajor.getDouble(statName),
					jsonUrbanMinor.getDouble(statName),
					jsonUrbanLocal.getDouble(statName),
					};
		}
		
		private int getIntOnStringScore(String score){
			switch (score) {
			case "ideal":
				return 3;
			case "average":
				return 2;
			case "high":
				return 1;
			case "unknown":
				return 0;
			default:
				return 0;
			}
		}
	}

}
