package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modusgo.ubi.customviews.PieChartView;
import com.modusgo.ubi.customviews.PieChartView.PieSector;

@SuppressLint("ValidFragment")
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
	
	float x = 0;
	float offsetX = 0;
	float viewHideOffset = 0;
	
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
		    
		    if(viewHideOffset==0){
		    	viewHideOffset = tvMarker.getCompoundDrawables()[0].getBounds().width()*0.685f;
		    }
		    
		    final int fi = i;
		    
		    tvMarker.setOnTouchListener(new View.OnTouchListener() { 
	            @Override
	            public boolean onTouch(View v, MotionEvent event){
	            	switch(event.getAction()){
	            		case MotionEvent.ACTION_DOWN:
	            			offsetX = 0;
	            			x = event.getX();
	            			break;
	            		case MotionEvent.ACTION_MOVE:
	            			offsetX = event.getX()-x;
	            			break;
	            		case MotionEvent.ACTION_UP:
	            			if(offsetX>10){
	            				if(!isVisible[fi]){
	            					showMarker(tvMarker, 500);
	        						isVisible[fi] = true;
	        					}
	            			}
	            			else if(offsetX<-10){
	            				if(isVisible[fi]){
	            					hideMarker(tvMarker, 500);
	        						isVisible[fi] = false;
	        					}	        					
	            			}
	            			else if(offsetX==0){
	            				if(isVisible[fi]){
	            					hideMarker(tvMarker, 500);
	        						isVisible[fi] = false;
	        					}
	            				else{
	            					showMarker(tvMarker, 500);
	        						isVisible[fi] = true;
	            				}
	            			}
	            			break;
	            	}
	            	updateChart(pieChart, true);
	            	
					return true;
	            }
		    });
		    
		    Drawable img = getResources().getDrawable( R.drawable.list_marker );
		    img.mutate();
		    
		    img.setColorFilter(getResources().getColor(backgroundResources[i]),PorterDuff.Mode.MULTIPLY);
			tvMarker.setTextColor(getResources().getColor(backgroundResources[i]));
			tvMarker.setText(names[i]);
		  	
		    tvMarker.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		    if(!isVisible[i]){
				hideMarker(tvMarker, 0);
			}
		    
		    markersLayout.addView(tvMarker);   	
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
	    outState.putStringArray(SAVED_NAMES, names);
	    outState.putString(SAVED_TITLE, title);
	}
}
