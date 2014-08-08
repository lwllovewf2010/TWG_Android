package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandableHeightGridView;

public class ScoreFragment extends Fragment{
	
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_TITLE = "title";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		Driver driver = DbHelper.getDrivers().get(getArguments().getInt("id", 0));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		ExpandableHeightGridView gvPercentData = (ExpandableHeightGridView) rootView.findViewById(R.id.gvPercentData);
		//gvPercentData.setColumnWidth(100);
		gvPercentData.setNumColumns(3);
		gvPercentData.setAdapter(getPercentInfoAdapter());
		gvPercentData.setExpanded(true);
		
		fillAdditionalInfo((LinearLayout)rootView.findViewById(R.id.llValue), inflater);
		
		return rootView;
	}
	
	private SimpleAdapter getPercentInfoAdapter(){
		int[] percents = new int[]{35,55,35,15,65,35,35};
		String[] titles = new String[]{"Use of speed", "Anticipation", "Aggression", "Smoothness", "Completeness", "Consistency", "Time on\nfamiliar road"};
		
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(titles.length);
		Map<String, Object> m;
		for (int i = 0; i < titles.length; i++) {
			m = new HashMap<String, Object>();
			m.put(ATTRIBUTE_NAME_VALUE, percents[i]);
			m.put(ATTRIBUTE_NAME_TITLE, titles[i]);
			data.add(m);
		}
		
		String[] from = new String[]{ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_TITLE};
		int[] to = new int[]{R.id.tvPercentValue, R.id.tvTitle};
		
		return new SimpleAdapter(getActivity(), data, R.layout.score_percents_item, from, to);
	}
	
	private void fillAdditionalInfo(LinearLayout additionalInfoLayout, LayoutInflater inflater){
		String[] values = new String[]{"July 14, 2014", "1GXEK4538960L23", "July 15, 2014", "January 1, 2014", "4,698 Miles", "11,475 Miles"};
		String[] titles = new String[]{"Last trip", "VIN", "Profile date", "Start date", "Profile driving miles", "Estimated annual driving"};
		
		for (int i = 0; i < titles.length; i++) {
			LinearLayout item = (LinearLayout)inflater.inflate(R.layout.score_additional_info_item, additionalInfoLayout, false);
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setAllCaps(true);
			tvTitle.setText(titles[i]);
			((TextView)item.findViewById(R.id.tvValue)).setText(values[i]);
			additionalInfoLayout.addView(item);
		}
		View line = new View(getActivity());
		line.setBackgroundColor(Color.parseColor("#acb1b7"));
		line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
		additionalInfoLayout.addView(line);
	}

}
