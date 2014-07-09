package com.modusgo.ubi;

import java.text.DecimalFormat;

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

public class ChartFragment extends TitledFragment {

	private final static String SAVED_PERCENTS = "columnPercents";
	private final static String SAVED_VALUES = "values";
	private final static String SAVED_TITLE = "title";
	private final static String SAVED_NAMES = "names";
	private final static int MAX_COLUMNS = 6;
	final String LOG_TAG = "myLogs";
	float[] columnPercents;
	float[] columnValues;
	int visibleColumns = 0;
	int[] backgroundResources;
	String names[];
	
	private LinearLayout left_nl;
	private LinearLayout right_nl;
	
	float x = 0;
	float offsetX = 0;
	float viewHideOffset = 0;
	
	public ChartFragment() {
	}
	
	public ChartFragment(String title, float[] values, String[] names) {
		this.title = title;
		this.names = names;
		
		float maxValue = 0;
		columnPercents = new float[values.length+1];
		columnValues = new float[values.length+1];
		
		for (float f : values) {
			maxValue = f>maxValue ? f : maxValue;
			columnValues[values.length]+=f;
		}
		columnValues[values.length]/=values.length;
		columnPercents[values.length] = columnValues[values.length]/maxValue;
		
		for (int i = 0; i < values.length; i++) {
			columnValues[i] = values[i];
			columnPercents[i] = columnValues[i]/maxValue;
		}
		
		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    if(savedInstanceState!=null){
            columnPercents = savedInstanceState.getFloatArray(SAVED_PERCENTS);
            columnValues = savedInstanceState.getFloatArray(SAVED_VALUES);
            names = savedInstanceState.getStringArray(SAVED_NAMES);
            title = savedInstanceState.getString(SAVED_TITLE);
	    }
	    
	    backgroundResources = new int[]{R.color.red,R.color.green,R.color.orange,R.color.blue,R.color.yellow,R.color.white};
	    
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.chart_fragment, null);
	    LinearLayout ll = (LinearLayout)rootView.findViewById(R.id.chart);
	    LinearLayout markersLayout = (LinearLayout)rootView.findViewById(R.id.markers);
	    
	    Typeface robotoThin = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
	    Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    
	    DecimalFormat df = new DecimalFormat("0");
	    
	    visibleColumns = columnPercents.length;
	    
	    left_nl = (LinearLayout) inflater.inflate(R.layout.chart_column, null);
    	LayoutParams leftRight_nl_p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,0);
    	leftRight_nl_p.width = 0;
    	left_nl.setLayoutParams(leftRight_nl_p);
    	left_nl.setVisibility(View.INVISIBLE);
    	
    	right_nl = (LinearLayout) inflater.inflate(R.layout.chart_column, null);
    	right_nl.setLayoutParams(leftRight_nl_p);
    	right_nl.setVisibility(View.INVISIBLE);
	  	
    	ll.addView(left_nl);
	    
	    for (int i = 0; i < columnPercents.length; i++) {
	    	final LinearLayout nl = (LinearLayout) inflater.inflate(R.layout.chart_column, null);
	    	LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,1f);
	    	p.width = 0;
	    	
		  	nl.setLayoutParams(p);
		    
		    TextView tv = ((TextView)nl.findViewById(R.id.textView));
		    tv.setTypeface(robotoThin);
		    tv.setText(df.format(columnValues[i]));
		    View column = nl.findViewById(R.id.view);
		    LinearLayout.LayoutParams p2 = (LinearLayout.LayoutParams) column.getLayoutParams();
		  	p2.weight = columnPercents[i];
		  	column.setLayoutParams(p2);
		  	
		    
		    
		    final TextView tvMarker = (TextView)inflater.inflate(R.layout.chart_marker_text, null);
		    LayoutParams p3 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,1f);
	    	p3.height = 0;
		    tvMarker.setLayoutParams(p3);
		    tvMarker.setTypeface(robotoLight);
		   
		    if(viewHideOffset==0){
		    	viewHideOffset = tvMarker.getCompoundDrawables()[0].getBounds().width()*0.685f;
		    }
		    
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
	            				if(nl.getVisibility()==View.GONE){
	            					showMarker(tvMarker, 500);
	            					nl.setVisibility(View.VISIBLE);
	        					}
	            			}
	            			else if(offsetX<-10){
	            				if(nl.getVisibility()==View.VISIBLE && visibleColumns>2){
	            					hideMarker(tvMarker, 500);
	            					nl.setVisibility(View.GONE);
	        					}	        					
	            			}
	            			else if(offsetX==0){
	            				if(nl.getVisibility()==View.VISIBLE){
	            					if(visibleColumns>2){
		            					hideMarker(tvMarker, 500);
		            					nl.setVisibility(View.GONE);
	            					}
	        					}
	            				else{
	            					showMarker(tvMarker, 500);
	            					nl.setVisibility(View.VISIBLE);
	            				}
	            			}
	            			break;
	            	}
	            	updateColumnsWidth();
					return true;
	            }
		    });
		    
		    Drawable img = getResources().getDrawable( R.drawable.list_marker );
		    img.mutate();
		    
		    if(i==columnPercents.length-1){
		  		column.setBackgroundResource(backgroundResources[backgroundResources.length-1]);
			    img.setColorFilter(getResources().getColor(backgroundResources[backgroundResources.length-1]),PorterDuff.Mode.MULTIPLY);
			    tvMarker.setTextColor(getResources().getColor(backgroundResources[backgroundResources.length-1]));		  		
		    }
		  	else{
		  		column.setBackgroundResource(backgroundResources[i]);
			    img.setColorFilter(getResources().getColor(backgroundResources[i]),PorterDuff.Mode.MULTIPLY);
			    tvMarker.setTextColor(getResources().getColor(backgroundResources[i]));
			    tvMarker.setText(names[i]);
		  	}
		    tvMarker.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
		    if(nl.getVisibility()==View.GONE){
				hideMarker(tvMarker, 0);
			}
		    ll.addView(nl);
		    markersLayout.addView(tvMarker);
		}
	    
	    ll.addView(right_nl);
	    
	    updateColumnsWidth();
	    
	    return  rootView;
	}
	
	private void updateColumnsWidth(){		
		if(visibleColumns<MAX_COLUMNS-1){
			LayoutParams leftRight_nl_p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,((float)(MAX_COLUMNS-1-visibleColumns))/2f);
	    	leftRight_nl_p.width = 0;
	    	left_nl.setLayoutParams(leftRight_nl_p);
	    	right_nl.setLayoutParams(leftRight_nl_p);
		}
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
	    
	    visibleColumns--;
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
	    
	    visibleColumns++;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    outState.putFloatArray(SAVED_PERCENTS, columnPercents);
	    outState.putFloatArray(SAVED_VALUES, columnValues);
	    outState.putStringArray(SAVED_NAMES, names);
	    outState.putString(SAVED_TITLE, title);
	}
}
