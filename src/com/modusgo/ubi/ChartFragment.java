package com.modusgo.ubi;

import java.text.DecimalFormat;

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

public class ChartFragment extends TitledFragment {

	private final static String SAVED_PERCENTS = "columnPercents";
	private final static String SAVED_VALUES = "values";
	private final static String SAVED_TITLE = "title";
	private final static String SAVED_NAMES = "names";
	final String LOG_TAG = "myLogs";
	float[] columnPercents;
	float[] columnValues;
	int[] backgroundResources;
	String names[];
	
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
		    tvMarker.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(nl.getVisibility()==View.VISIBLE){
						//tvMarker.setAlpha(0.3f); - Do not use, not compatible with API <11
						tvMarker.setTextColor(tvMarker.getTextColors().withAlpha(76));
						for (Drawable d : tvMarker.getCompoundDrawables()) {
							if(d!=null) d.setAlpha(76);
						}
						nl.setVisibility(View.GONE);
					}
					else{
						tvMarker.setTextColor(tvMarker.getTextColors().withAlpha(255));
						for (Drawable d : tvMarker.getCompoundDrawables()) {
							if(d!=null) d.setAlpha(255);
						}
						nl.setVisibility(View.VISIBLE);
					}
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
		    ll.addView(nl);
		    markersLayout.addView(tvMarker);
		}
	    
	    return  rootView;
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
