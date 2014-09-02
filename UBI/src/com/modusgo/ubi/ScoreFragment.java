package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandableHeightGridView;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ScoreFragment extends Fragment{
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_TITLE = "title";
	
	FragmentTabHost tabHost;
	
	LayoutInflater inflater;
	
	TextView tvScore;
	LinearLayout llAdditionalData;
	
	SimpleAdapter percentInfoAdapter;
	ArrayList<Map<String, Object>> percentInfoData;
	
	RadioGroup rgPieCharts;
	RadioGroup rgCircles;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		this.inflater = inflater;
		
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
		
		tvScore = (TextView)rootView.findViewById(R.id.tvScore);
		((View)tvScore.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), EducationActivity.class);
				i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, R.string.your_score);
				startActivity(i);
			}
		});
		
		ExpandableHeightGridView gvPercentData = (ExpandableHeightGridView) rootView.findViewById(R.id.gvPercentData);
		//gvPercentData.setColumnWidth(100);
		gvPercentData.setNumColumns(3);
		percentInfoData = new ArrayList<Map<String, Object>>();
		updatePercentInfoAdapter(new int[]{0,0,0,0,0,0});
		gvPercentData.setAdapter(percentInfoAdapter);
		gvPercentData.setAdditionalTextExpand(1, 12.5f);
		gvPercentData.setExpanded(true);
		gvPercentData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int infoStringResource = -1;
				switch (position) {
				case 0:
					infoStringResource = R.string.use_of_speed;
					break;
				case 1:
					infoStringResource = R.string.anticipation;
					break;
				case 2:
					infoStringResource = R.string.aggression;
					break;
				case 3:
					infoStringResource = R.string.smoothness;
					break;
				case 4:
					infoStringResource = R.string.completeness;
					break;
				case 5:
					infoStringResource = R.string.consistency;
					break;
				}
				if(infoStringResource != -1){
					Intent i = new Intent(getActivity(), EducationActivity.class);
					i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, infoStringResource);
					startActivity(i);
				}
			}
		});
		
		llAdditionalData = (LinearLayout)rootView.findViewById(R.id.llValue);
		
		/*----------------------------------------PIE CHARTS------------------------------------------*/
		
		rgPieCharts = (RadioGroup)rootView.findViewById(R.id.radioGroupPieCharts);

		/*----------------------------------------CIRCLES INFO------------------------------------------*/
		rgCircles = (RadioGroup)rootView.findViewById(R.id.radioGroupCircles);
        
        
        new GetScoreTask(getActivity()).execute("drivers/"+driver.id+"/score.json");
        
		return rootView;
	}
	
	private void updatePercentInfoAdapter(int[] values){
		String[] titles = new String[]{"Use of speed", "Anticipation", "Aggression", "Smoothness", "Completeness", "Consistency"};
		
		percentInfoData.clear();
		Map<String, Object> m;
		for (int i = 0; i < titles.length; i++) {
			m = new HashMap<String, Object>();
			m.put(ATTRIBUTE_NAME_VALUE, values[i]);
			m.put(ATTRIBUTE_NAME_TITLE, titles[i]);
			percentInfoData.add(m);
		}
		
		String[] from = new String[]{ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_TITLE};
		int[] to = new int[]{R.id.tvPercentValue, R.id.tvTitle};
		
		if(percentInfoAdapter==null)
			percentInfoAdapter = new SimpleAdapter(getActivity(), percentInfoData, R.layout.score_percents_item, from, to);
		else
			percentInfoAdapter.notifyDataSetChanged();
	}
	
	private void fillAdditionalInfo(String[] values){
		String[] titles = new String[]{"Last trip", "VIN", "Profile date", "Start date", "Profile driving miles", "Estimated annual driving"};
		
		for (int i = 0; i < titles.length; i++) {
			LinearLayout item = (LinearLayout)inflater.inflate(R.layout.score_additional_info_item, llAdditionalData, false);
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setAllCaps(true);
			tvTitle.setText(titles[i]);
			((TextView)item.findViewById(R.id.tvValue)).setText(values[i]);
			llAdditionalData.addView(item);
		}
		View line = new View(getActivity());
		line.setBackgroundColor(Color.parseColor("#acb1b7"));
		line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
		llAdditionalData.addView(line);
	}
	
	private void udpatePieCharts(float[] roadSetting, float[] roadType, float[] timeOfDay){
		
		String pieChartTabs[] = new String[]{"TIME OF DAY", "ROAD SETTING", "ROAD TYPE"};
		
		Bundle bundles[] = new Bundle[3];
		bundles[0] = new Bundle();
	    bundles[0].putFloatArray(PieChartFragment.SAVED_VALUES, timeOfDay);
	    bundles[0].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{
	    		Math.round(timeOfDay[0])+"% WEEKDAY",
	        	Math.round(timeOfDay[1])+"% WEEKDAY",
	        	Math.round(timeOfDay[2])+"% WEEKEND",
	        	Math.round(timeOfDay[3])+"% WEEKDAY",
	        	Math.round(timeOfDay[4])+"% WEEKDAY",
	        	Math.round(timeOfDay[5])+"% WEEKDAY"});
	    bundles[0].putStringArray(PieChartFragment.SAVED_SUBTITLES, new String[]{"6:30 AM - 9:30 AM","4:00 PM - 7:00 PM","All day","9:30 AM - 4:00 PM","7:00 PM - 11:59 PM","12:00 AM - 6:30 AM"});
		    
		bundles[1] = new Bundle();
		bundles[1].putFloatArray(PieChartFragment.SAVED_VALUES, roadSetting);
		bundles[1].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{
				Math.round(roadSetting[0])+"%\nRURAL",
				Math.round(roadSetting[1])+"%\nSUBURBAN",
				Math.round(roadSetting[2])+"%\nURBAN"});
	    
        bundles[2] = new Bundle();
        bundles[2].putFloatArray(PieChartFragment.SAVED_VALUES, roadType);
        bundles[2].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{
        		Math.round(roadType[0])+"%\nMAJOR ROAD",
        		Math.round(roadType[1])+"%\nLOCAL ROAD",
        		Math.round(roadType[2])+"%\nHIGHWAY",
        		Math.round(roadType[3])+"%\nMINOR ROAD"});
	   
        ArrayList<Fragment> pieChartFragments = new ArrayList<>();
        
        for (int i = 0; i < pieChartTabs.length; i++) {
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgPieCharts, false);
        	rb.setText(pieChartTabs[i]);
        	rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
        	Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
        	rb.setTypeface(tf);
        	
        	final Fragment fragment = new PieChartFragment();
        	fragment.setArguments(bundles[i]);
        	pieChartFragments.add(fragment);
        	
        	rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        		@Override
        		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        			if(isChecked){
        				getChildFragmentManager().beginTransaction()
        				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        				.replace(R.id.pieChartsContainer, fragment)
        				.commit();
        			}
        		}
        	});
        	
        	rgPieCharts.addView(rb);
        	if(i==0){
        		rb.setId(R.id.radioButtonSelected);
        		rgPieCharts.check(rb.getId());
        		
        		getChildFragmentManager().beginTransaction()
        		.replace(R.id.pieChartsContainer, fragment)
        		.commitAllowingStateLoss();
        	}
        }
	}
	
	private void updateCircles(Bundle bundles[]) {
		String circleTabs[] = new String[]{"Suburban", "Urban", "Rural"};
        ArrayList<Fragment> circleFragments = new ArrayList<>();
        
        for (int i = 0; i < circleTabs.length; i++) {
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgCircles, false);
        	rb.setText(circleTabs[i]);
            rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
            rb.setTypeface(tf);
            
            final Fragment fragment = new CirclesFragment();
            bundles[i].putString(TitledFragment.SAVED_TITLE, circleTabs[i]);
            fragment.setArguments(bundles[i]);
            circleFragments.add(fragment);
            
            rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						getChildFragmentManager().beginTransaction()
						.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				        .replace(R.id.circlesContainer, fragment)
				        .commit();
					}
				}
			});
            
            rgCircles.addView(rb);
            if(i==0){
                rb.setId(R.id.radioButtonSelected);
                rgCircles.check(rb.getId());
                
                getChildFragmentManager().beginTransaction()
                .replace(R.id.circlesContainer, fragment)
                .commitAllowingStateLoss();
            }
		}
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
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject json) throws JSONException {
			System.out.println(json);
			
			tvScore.setText(json.getString("grade"));
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
			DecimalFormat df = new DecimalFormat("0.000");
			
			String[] values = new String[]{
					Utils.convertTime(driver.lastTripDate, sdf), 
					"1GXEK4538960L23", 
					Utils.convertTime(json.getString("created"), sdf), 
					Utils.convertTime(json.getString("startdate"), sdf), 
					df.format(json.getDouble("summary_distance"))+" Miles", 
					df.format(json.getDouble("summary_ead"))+" Miles"};
			fillAdditionalInfo(values);
			
			
			updatePercentInfoAdapter(new int[]{
					json.getInt("score_pace"),
					json.getInt("score_anticipation"),
					json.getInt("score_aggression"),
					json.getInt("score_smoothness"),
					json.getInt("score_completeness"),
					json.getInt("score_consistency")
			});
			
			udpatePieCharts(
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
					}
					);
			
			JSONObject jsonMarks = json.getJSONObject("road_env_analysis");
			
			JSONObject jsonStats = json.getJSONObject("road_env_stats");
			
			Bundle b[] = new Bundle[]{
					getPageBundle("suburban", jsonMarks, jsonStats),
					getPageBundle("urban", jsonMarks, jsonStats),
					getPageBundle("rural", jsonMarks, jsonStats)
			};
			
			updateCircles(b);
			
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
