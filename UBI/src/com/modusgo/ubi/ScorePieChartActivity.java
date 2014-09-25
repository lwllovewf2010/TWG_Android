package com.modusgo.ubi;

import java.util.ArrayList;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ScorePieChartActivity extends MainActivity{

	public static final String SAVED_PIE_CHART_ROAD_SETTINGS = "pieChartRoadSettings";
	public static final String SAVED_PIE_CHART_ROAD_TYPE = "pieChartRoadType";
	public static final String SAVED_PIE_CHART_TIME_OF_DAY = "pieChartTimeOfDay";
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;

	float[] pieChartRoadSettings;
	float[] pieChartRoadType;
	float[] pieChartTimeOfDay;
	RadioGroup rgPieCharts;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_pie_chart);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Time/Road Charts");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			pieChartRoadSettings = savedInstanceState.getFloatArray(SAVED_PIE_CHART_ROAD_SETTINGS);
			pieChartRoadType = savedInstanceState.getFloatArray(SAVED_PIE_CHART_ROAD_TYPE);
			pieChartTimeOfDay = savedInstanceState.getFloatArray(SAVED_PIE_CHART_TIME_OF_DAY);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
			pieChartRoadSettings = getIntent().getFloatArrayExtra(SAVED_PIE_CHART_ROAD_SETTINGS);
			pieChartRoadType = getIntent().getFloatArrayExtra(SAVED_PIE_CHART_ROAD_TYPE);
			pieChartTimeOfDay = getIntent().getFloatArrayExtra(SAVED_PIE_CHART_TIME_OF_DAY);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		rgPieCharts = (RadioGroup) findViewById(R.id.radioGroupPieCharts);
		updatePieCharts(pieChartRoadSettings, pieChartRoadType, pieChartTimeOfDay);
        
	}
	
	private void updatePieCharts(float[] roadSetting, float[] roadType, float[] timeOfDay){
		
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
        
        LayoutInflater inflater = getLayoutInflater();
        
        for (int i = 0; i < pieChartTabs.length; i++) {
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgPieCharts, false);
        	rb.setText(pieChartTabs[i]);
        	rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
        	Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
        	rb.setTypeface(tf);
        	
        	final Fragment fragment = new PieChartFragment();
        	fragment.setArguments(bundles[i]);
        	pieChartFragments.add(fragment);
        	
        	rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        		@Override
        		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        			if(isChecked){
        				getSupportFragmentManager().beginTransaction()
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
        		
        		getSupportFragmentManager().beginTransaction()
        		.replace(R.id.pieChartsContainer, fragment)
        		.commitAllowingStateLoss();
        	}
        }
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putFloatArray(SAVED_PIE_CHART_ROAD_SETTINGS, pieChartRoadSettings);
		outState.putFloatArray(SAVED_PIE_CHART_ROAD_TYPE, pieChartRoadType);
		outState.putFloatArray(SAVED_PIE_CHART_TIME_OF_DAY, pieChartTimeOfDay);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void up() {
		super.up();
		overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
	}

}
