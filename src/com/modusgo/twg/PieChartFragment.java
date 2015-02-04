package com.modusgo.twg;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modusgo.twg.R;
import com.modusgo.twg.customviews.PieChartView;
import com.modusgo.twg.customviews.PieChartView.PieSector;

@SuppressLint("ValidFragment")
public class PieChartFragment extends TitledFragment{

	public final static String SAVED_VALUES = "values";
	public final static String SAVED_SUBTITLES = "title";
	public final static String SAVED_TITLES = "names";
	public final static String SAVED_COLORS = "colorIds";
	final String LOG_TAG = "myLogs";
	float[] chartValues;
	String[] titles;
	String[] subTitles;
	int[] colors;
	
	float x = 0;
	float offsetX = 0;
	float viewHideOffset = 0;
	
	public PieChartFragment() {
	}
	
	public PieChartFragment(float[] values, String[] titles, String[] subTitles) {
		this.titles = titles;
		this.subTitles = subTitles;
		this.chartValues = values;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    if(savedInstanceState!=null){
            chartValues = savedInstanceState.getFloatArray(SAVED_VALUES);
            titles = savedInstanceState.getStringArray(SAVED_TITLES);
            subTitles = savedInstanceState.getStringArray(SAVED_SUBTITLES);
            colors = savedInstanceState.getIntArray(SAVED_COLORS);
	    }
	    else if(getArguments()!=null){    	
    		titles = getArguments().getStringArray(SAVED_TITLES);
    		subTitles = getArguments().getStringArray(SAVED_SUBTITLES);	
    		chartValues = getArguments().getFloatArray(SAVED_VALUES);
    		colors = getArguments().getIntArray(SAVED_COLORS);
	    }
	    
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.pie_chart_fragment, container, false);
    	final PieChartView pieChart = (PieChartView) rootView.findViewById(R.id.chart);
	    LinearLayout markersLayout = (LinearLayout)rootView.findViewById(R.id.markers);
	    
	    pieChart.setChartSectors(new PieSector[]{pieChart.new PieSector(100,getResources().getColor(R.color.white))});
	    
	    for (int i = 0; i < chartValues.length; i++) {
		    
		    LinearLayout llMarker = (LinearLayout)inflater.inflate(R.layout.chart_marker_text, markersLayout, false);
		    TextView tvMarkerTitle = (TextView) llMarker.findViewById(R.id.tvTitle);
		    tvMarkerTitle.setTextColor(colors[i]);
			tvMarkerTitle.setText(titles[i]);
			TextView tvMarkerSubTitle = (TextView) llMarker.findViewById(R.id.tvSubTitle);
			tvMarkerSubTitle.setTextColor(colors[i]);
			if(subTitles!=null && subTitles[i]!=null)
				tvMarkerSubTitle.setText(subTitles[i]);
			else
				tvMarkerSubTitle.setVisibility(View.GONE);
		  	
		    markersLayout.addView(llMarker);   	
	    }
	    updateChart(pieChart, false);
	    
	    return  rootView;
	}
	
	private void updateChart(PieChartView pieChart, boolean animate){
		ArrayList<PieSector> pieSectors = new ArrayList<PieChartView.PieSector>();
		for (int i = 0; i < chartValues.length; i++) {
			if(chartValues[i]>=0.5){
			    	pieSectors.add(pieChart.new PieSector(chartValues[i],colors[i]));
			}
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
	    outState.putFloatArray(SAVED_VALUES, chartValues);
	    outState.putStringArray(SAVED_TITLES, titles);
	    outState.putStringArray(SAVED_SUBTITLES, subTitles);
	    outState.putIntArray(SAVED_COLORS, colors);
	}
}
