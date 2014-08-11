package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modusgo.ubi.customviews.PieChartView;
import com.modusgo.ubi.customviews.PieChartView.PieSector;

@SuppressLint("ValidFragment")
public class PieChartFragment extends TitledFragment{

	public final static String SAVED_VISIBILITIES = "visibilities";
	public final static String SAVED_VALUES = "values";
	public final static String SAVED_SUBTITLES = "title";
	public final static String SAVED_TITLES = "names";
	final String LOG_TAG = "myLogs";
	float[] chartValues;
	int[] backgroundResources;
	String[] titles;
	String[] subTitles;
	boolean[] isVisible;
	
	float x = 0;
	float offsetX = 0;
	float viewHideOffset = 0;
	
	public PieChartFragment() {
	}
	
	public PieChartFragment(float[] values, String[] titles, String[] subTitles) {
		this.titles = titles;
		this.subTitles = subTitles;
		this.chartValues = values;
		isVisible = new boolean[chartValues.length];
		Arrays.fill(isVisible, Boolean.TRUE);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    if(savedInstanceState!=null){
            isVisible = savedInstanceState.getBooleanArray(SAVED_VISIBILITIES);
            chartValues = savedInstanceState.getFloatArray(SAVED_VALUES);
            titles = savedInstanceState.getStringArray(SAVED_TITLES);
            subTitles = savedInstanceState.getStringArray(SAVED_SUBTITLES);
	    }
	    else if(getArguments()!=null){    	
    		titles = getArguments().getStringArray(SAVED_TITLES);
    		subTitles = getArguments().getStringArray(SAVED_SUBTITLES);	
    		chartValues = getArguments().getFloatArray(SAVED_VALUES);
            isVisible = new boolean[chartValues.length];
    		Arrays.fill(isVisible, Boolean.TRUE);
	    }
	    
	    backgroundResources = new int[]{R.color.pie_black,R.color.pie_green,R.color.pie_red,R.color.pie_gray,R.color.pie_orange,R.color.pie_blue};
	    
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.pie_chart_fragment, container, false);
    	final PieChartView pieChart = (PieChartView) rootView.findViewById(R.id.chart);
	    LinearLayout markersLayout = (LinearLayout)rootView.findViewById(R.id.markers);
	    
	    pieChart.setChartSectors(new PieSector[]{pieChart.new PieSector(10,R.color.white),pieChart.new PieSector(17,R.color.green),pieChart.new PieSector(17,R.color.blue)});
	    
	    for (int i = 0; i < chartValues.length; i++) {
		    
		    LinearLayout llMarker = (LinearLayout)inflater.inflate(R.layout.chart_marker_text, markersLayout, false);
		    TextView tvMarkerTitle = (TextView) llMarker.findViewById(R.id.tvTitle);
		    tvMarkerTitle.setTextColor(getResources().getColor(backgroundResources[i]));
			tvMarkerTitle.setText(titles[i]);
			TextView tvMarkerSubTitle = (TextView) llMarker.findViewById(R.id.tvSubTitle);
			tvMarkerSubTitle.setTextColor(getResources().getColor(backgroundResources[i]));
			if(subTitles!=null && subTitles[i]!=null)
				tvMarkerSubTitle.setText(subTitles[i]);
			else
				tvMarkerSubTitle.setVisibility(View.GONE);
		  	
		    markersLayout.addView(llMarker);   	
	    }
	    updateChart(pieChart, false);
	    
	    return  rootView;
	}
	
	private void hideMarker(View view, long duration)
	{
	    TranslateAnimation anim = new TranslateAnimation( 0, -viewHideOffset , 0,0);
	    AlphaAnimation alphaAnim = new AlphaAnimation(1, 0.3f);
	    
	    AnimationSet as = new AnimationSet(true);
	    as.addAnimation(anim);
	    as.addAnimation(alphaAnim);
	    as.setFillAfter(true);
	    as.setDuration(duration);
	    
	    view.startAnimation(as);
	}
	private void showMarker(View view, long duration)
	{
	    TranslateAnimation anim = new TranslateAnimation( -viewHideOffset, 0 , 0,0);	    
	    AlphaAnimation alphaAnim = new AlphaAnimation(0.3f, 1);
	    
	    AnimationSet as = new AnimationSet(true);
	    as.addAnimation(anim);
	    as.addAnimation(alphaAnim);
	    as.setFillAfter(true);
	    as.setDuration(duration);
	    
	    view.startAnimation(as);
	}
	
	private void updateChart(PieChartView pieChart, boolean animate){
		ArrayList<PieSector> pieSectors = new ArrayList<PieChartView.PieSector>();
		for (int i = 0; i < chartValues.length; i++) {
			if(isVisible[i])
		    	pieSectors.add(pieChart.new PieSector(chartValues[i],backgroundResources[i]));
			else
		    	pieSectors.add(pieChart.new PieSector(0,backgroundResources[i]));
		}
		PieSector pieSectorsArr[] = new PieSector[pieSectors.size()];
		
		if(!animate)
		    pieChart.setChartSectors(pieSectors.toArray(pieSectorsArr));
		else
			pieChart.animateChartSectors(pieSectors.toArray(pieSectorsArr));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBooleanArray(SAVED_VISIBILITIES, isVisible);
	    outState.putFloatArray(SAVED_VALUES, chartValues);
	    outState.putStringArray(SAVED_TITLES, titles);
	    outState.putStringArray(SAVED_SUBTITLES, subTitles);
	}
}
