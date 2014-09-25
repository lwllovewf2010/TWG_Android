package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandableHeightGridView;

public class ScoreInfoActivity extends MainActivity{
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_TITLE = "title";
	
	public static final String SAVED_ADDITIONAL_DATA = "additionalData";
	public static final String SAVED_PERCENTAGE_DATA = "percentageData"; 
	
	LinearLayout llAdditionalData;
	SimpleAdapter percentInfoAdapter;

	ArrayList<Map<String, Object>> percentInfoData;
	String[] additionalData;
	int[] percentageData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_score_info);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("SCORE");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			additionalData = savedInstanceState.getStringArray(SAVED_ADDITIONAL_DATA);
			percentageData = savedInstanceState.getIntArray(SAVED_PERCENTAGE_DATA);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id", 0);
			additionalData = getIntent().getStringArrayExtra(SAVED_ADDITIONAL_DATA);
			percentageData = getIntent().getIntArrayExtra(SAVED_PERCENTAGE_DATA);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		ExpandableHeightGridView gvPercentData = (ExpandableHeightGridView) findViewById(R.id.gvPercentData);
		//gvPercentData.setColumnWidth(100);
		gvPercentData.setNumColumns(3);
		
		percentInfoData = new ArrayList<Map<String, Object>>();
		updatePercentInfoAdapter(percentageData);
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
					Intent i = new Intent(ScoreInfoActivity.this, EducationActivity.class);
					i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, infoStringResource);
					startActivity(i);
				}
			}
		});
		
		llAdditionalData = (LinearLayout) findViewById(R.id.llValue);
		fillAdditionalInfo(additionalData);
        
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
			percentInfoAdapter = new SimpleAdapter(this, percentInfoData, R.layout.score_percents_item, from, to);
		else
			percentInfoAdapter.notifyDataSetChanged();
	}
	
	private void fillAdditionalInfo(String[] values){
		String[] titles = new String[]{"Last trip", "VIN", "Profile date", "Start date", "Profile driving miles", "Estimated annual driving"};
		
		LayoutInflater inflater = getLayoutInflater();
		
		for (int i = 0; i < titles.length; i++) {
			LinearLayout item = (LinearLayout)inflater.inflate(R.layout.score_additional_info_item, llAdditionalData, false);
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setAllCaps(true);
			tvTitle.setText(titles[i]);
			((TextView)item.findViewById(R.id.tvValue)).setText(values[i]);
			llAdditionalData.addView(item);
		}
		View line = new View(this);
		line.setBackgroundColor(Color.parseColor("#acb1b7"));
		line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
		llAdditionalData.addView(line);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putStringArray(SAVED_ADDITIONAL_DATA, additionalData);
		outState.putIntArray(SAVED_PERCENTAGE_DATA, percentageData);
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
