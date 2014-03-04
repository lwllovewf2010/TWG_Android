package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modusgo.ubi.customviews.PieChartView;
import com.modusgo.ubi.customviews.PieChartView.PieSector;

public class PieChartFragment extends TitledFragment{

	private final static String SAVED_VISIBILITIES = "visibilities";
	private final static String SAVED_VALUES = "values";
	private final static String SAVED_TITLE = "title";
	private final static String SAVED_NAMES = "names";
	final String LOG_TAG = "myLogs";
	float[] chartValues;
	int[] backgroundResources;
	String[] names;
	boolean[] isVisible;
	
	public PieChartFragment() {
	}
	
	public PieChartFragment(String title, float[] values, String[] names) {
		this.title = title;
		this.names = names;
		this.chartValues = values;
		isVisible = new boolean[chartValues.length];
		Arrays.fill(isVisible, Boolean.TRUE);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    if(savedInstanceState!=null){
            isVisible = savedInstanceState.getBooleanArray(SAVED_VISIBILITIES);
            chartValues = savedInstanceState.getFloatArray(SAVED_VALUES);
            names = savedInstanceState.getStringArray(SAVED_NAMES);
            title = savedInstanceState.getString(SAVED_TITLE);
	    }
	    
	    backgroundResources = new int[]{R.color.red,R.color.green,R.color.orange,R.color.blue,R.color.yellow,R.color.white};
	    
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.pie_chart_fragment, null);
    	final PieChartView pieChart = (PieChartView) rootView.findViewById(R.id.chart);
	    LinearLayout markersLayout = (LinearLayout)rootView.findViewById(R.id.markers);
	    
	    pieChart.setChartSectors(new PieSector[]{pieChart.new PieSector(10,R.color.white),pieChart.new PieSector(17,R.color.green),pieChart.new PieSector(17,R.color.blue)});
	    
	    Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    
	    for (int i = 0; i < chartValues.length; i++) {
	    	
	    	LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,1f);
	    	p.width = 0;
		    
		    final TextView tvMarker = (TextView)inflater.inflate(R.layout.chart_marker_text, null);
		    LayoutParams p3 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,1f);
	    	p3.height = 0;
		    tvMarker.setLayoutParams(p3);
		    tvMarker.setTypeface(robotoLight);
		    
		    final int fi = i;
		    tvMarker.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(isVisible[fi]){
						//tvMarker.setAlpha(0.3f); - Do not use, not compatible with API <11
						tvMarker.setTextColor(tvMarker.getTextColors().withAlpha(76));
						for (Drawable d : tvMarker.getCompoundDrawables()) {
							if(d!=null) d.setAlpha(76);
						}
						isVisible[fi] = false;
					}
					else{
						tvMarker.setTextColor(tvMarker.getTextColors().withAlpha(255));
						for (Drawable d : tvMarker.getCompoundDrawables()) {
							if(d!=null) d.setAlpha(255);
						}
						isVisible[fi] = true;
					}
					updateChart(pieChart);
				}
			});
		    
		    Drawable img = getResources().getDrawable( R.drawable.list_marker );
		    img.mutate();
		    
		    img.setColorFilter(getResources().getColor(backgroundResources[i]),PorterDuff.Mode.MULTIPLY);
			tvMarker.setTextColor(getResources().getColor(backgroundResources[i]));
			tvMarker.setText(names[i]);
		  	
		    tvMarker.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		    if(!isVisible[i]){
				//tvMarker.setAlpha(0.3f); - Do not use, not compatible with API <11
				tvMarker.setTextColor(tvMarker.getTextColors().withAlpha(76));
				for (Drawable d : tvMarker.getCompoundDrawables()) {
					if(d!=null) d.setAlpha(76);
				}
			}
		    
		    markersLayout.addView(tvMarker);   	
	    }
	    updateChart(pieChart);
	    
	    return  rootView;
	}
	
	private void updateChart(PieChartView pieChart){
		ArrayList<PieSector> pieSectors = new ArrayList<PieChartView.PieSector>();
		for (int i = 0; i < chartValues.length; i++) {
			if(isVisible[i])
		    	pieSectors.add(pieChart.new PieSector(chartValues[i],backgroundResources[i]));
		}
		PieSector pieSectorsArr[] = new PieSector[pieSectors.size()];
	    pieChart.setChartSectors(pieSectors.toArray(pieSectorsArr));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBooleanArray(SAVED_VISIBILITIES, isVisible);
	    outState.putFloatArray(SAVED_VALUES, chartValues);
	    outState.putStringArray(SAVED_NAMES, names);
	    outState.putString(SAVED_TITLE, title);
	}
}
