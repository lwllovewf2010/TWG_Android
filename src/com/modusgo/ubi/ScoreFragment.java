package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandableHeightGridView;

public class ScoreFragment extends Fragment{
	
	private static final String ATTRIBUTE_NAME_VALUE = "value";
	private static final String ATTRIBUTE_NAME_TITLE = "title";
	
	FragmentTabHost tabHost;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		Driver driver = DbHelper.getDrivers().get(getArguments().getInt("id", 0));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);
		
		rootView.findViewById(R.id.btnMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		ExpandableHeightGridView gvPercentData = (ExpandableHeightGridView) rootView.findViewById(R.id.gvPercentData);
		//gvPercentData.setColumnWidth(100);
		gvPercentData.setNumColumns(3);
		gvPercentData.setAdapter(getPercentInfoAdapter());
		gvPercentData.setAdditionalTextExpand(1, 12.5f);
		gvPercentData.setExpanded(true);
		
		fillAdditionalInfo((LinearLayout)rootView.findViewById(R.id.llValue), inflater);
		
		/*----------------------------------------PIE CHARTS------------------------------------------*/
		
		RadioGroup rgPieCharts = (RadioGroup)rootView.findViewById(R.id.radioGroupPieCharts);
		String pieChartTabs[] = new String[]{"ROAD SETTING", "ROAD TYPE", "TIME OF DAY"};
		Bundle bundles[] = new Bundle[3];
		bundles[0] = new Bundle();
		bundles[0].putFloatArray(PieChartFragment.SAVED_VALUES, new float[]{57f,34f,9f});
		bundles[0].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{"57%\nRURAL","34%\nSUBURBAN","9%\nURBAN"});
	    
        bundles[1] = new Bundle();
        bundles[1].putFloatArray(PieChartFragment.SAVED_VALUES, new float[]{41f,22f,19f,18f});
        bundles[1].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{"41%\nMAJOR ROAD","22%\nLOCAL ROAD","19%\nHIGHWAY","18%\nMINOR ROAD"});
	    
        bundles[2] = new Bundle();
        bundles[2].putFloatArray(PieChartFragment.SAVED_VALUES, new float[]{35f,32f,15f,8f,7f,3f});
        bundles[2].putStringArray(PieChartFragment.SAVED_TITLES, new String[]{"35% WEEKDAY","32% WEEKDAY","15% WEEKEND","8% WEEKDAY","7% WEEKDAY","3% WEEKDAY"});
        bundles[2].putStringArray(PieChartFragment.SAVED_SUBTITLES, new String[]{"6:30 AM - 9:30 AM","4:00 PM - 7:00 PM","All day","9:30 AM - 4:00 PM","7:00 PM - 11:59 PM","12:00 AM - 6:30 AM"});
	    
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
						getFragmentManager().beginTransaction()
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
                
                getFragmentManager().beginTransaction()
                .replace(R.id.pieChartsContainer, fragment)
                .commit();
            }
		}

		/*----------------------------------------CIRCLES INFO------------------------------------------*/
        
        RadioGroup rgCircles = (RadioGroup)rootView.findViewById(R.id.radioGroupCircles);
        String circleTabs[] = new String[]{"Urban", "Suburban", "Rural"};
        ArrayList<Fragment> circleFragments = new ArrayList<>();
        
        for (int i = 0; i < circleTabs.length; i++) {
        	RadioButton rb = (RadioButton)inflater.inflate(R.layout.radio_tab, rgCircles, false);
        	rb.setText(circleTabs[i]);
            rb.setBackgroundResource(R.drawable.radio_tab_bg_selector);
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
            rb.setTypeface(tf);
            
            final Fragment fragment = new CirclesFragment();
            Bundle circleB = new Bundle();
            circleB.putString(TitledFragment.SAVED_TITLE, circleTabs[i]);
            fragment.setArguments(circleB);
            circleFragments.add(fragment);
            
            rb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						getFragmentManager().beginTransaction()
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
                
                getFragmentManager().beginTransaction()
                .replace(R.id.circlesContainer, fragment)
                .commit();
            }
		}
        
        
		
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
