package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.modusgo.demo.R;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.customviews.ExpandablePanel;
import com.modusgo.ubi.customviews.ExpandablePanel.OnExpandListener;

public class CirclesFragment extends TitledFragment{
	
	public static final String SAVED_SECTIONS = "sections";
	
	SharedPreferences prefs;
	
	static TextView tvPopup;
	boolean showTipPopup = true;
	
	ArrayList<CirclesSection> sections;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(savedInstanceState!=null){
			title = savedInstanceState.getString(SAVED_TITLE);
			sections = (ArrayList<CirclesSection>) savedInstanceState.getSerializable(SAVED_SECTIONS);
	    }
	    else if(getArguments()!=null){  
			title = getArguments().getString(SAVED_TITLE);
			sections = (ArrayList<CirclesSection>) getArguments().getSerializable(SAVED_SECTIONS);
	    }
		
		LinearLayout rootView = new LinearLayout(getActivity());
		rootView.setOrientation(LinearLayout.VERTICAL);
	    LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	    rootView.setLayoutParams(rootParams);
	    
	    for (CirclesSection section : sections) {
			rootView.addView(createNewRow(section, inflater));
		}	
		
	    prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    showTipPopup = prefs.getBoolean(Constants.PREF_SHOW_TIP_POPUP, true);
	    
		return rootView;
	}
	
	private void disableTipPopup(){
		prefs.edit().putBoolean(Constants.PREF_SHOW_TIP_POPUP, false).commit();
	    if(tvPopup!=null){
	    	((ViewGroup)tvPopup.getParent()).removeView(tvPopup);
	    	tvPopup = null;
	    }
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		if(showTipPopup){
			FrameLayout fl = (FrameLayout) getView().getParent();
	        
			if(tvPopup==null){
		        tvPopup = new TextView(getActivity()){
		        	@Override
		        	protected void onLayout(boolean changed, int left, int top,
		        			int right, int bottom) {
		        		setX(getResources().getDisplayMetrics().widthPixels*5/8-getMeasuredWidth()/2);
		        		setY(getHeight()*0.55f);
		        		
		        		super.onLayout(changed, left, top, right, bottom);
		        	}
		        };
		        
		        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-600-SemiBold.ttf");
	
		        tvPopup.setTypeface(typeface);
		        tvPopup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		        tvPopup.setText("Touch the dots for\nmore information");
		    	tvPopup.setBackgroundResource(R.drawable.score_circles_info_popup);
		    	tvPopup.setTextColor(Color.parseColor("#FFFFFF"));
		    	FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		    	tvPopup.setLayoutParams(params);
		    	int dp20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
		        int dp15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
		        tvPopup.setPadding(
		        		dp20, 
		        		dp15, 
		        		dp15, 
		        		dp15);
		    	
		        tvPopup.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						disableTipPopup();
					}
				});
		        if(fl!=null){
		        	fl.addView(tvPopup);
		        }
			}
    		tvPopup.bringToFront();
	        
		}
		super.onViewCreated(view, savedInstanceState);
	}
	
	private View createNewRow(CirclesSection section, LayoutInflater inflater){
		LinearLayout rootView = new LinearLayout(getActivity());
		rootView.setOrientation(LinearLayout.VERTICAL);
	    LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	    rootView.setLayoutParams(rootParams);
	    
	    final RowData rData = new RowData();
		
		String[] circleTitles = new String[]{"Highway", "Major Road", "Minor Road", "Local Road"};
	    
		LinearLayout circlesRow = (LinearLayout) inflater.inflate(R.layout.score_circles_row_item, rootView, false);
        ((TextView)circlesRow.findViewById(R.id.tvTitle)).setText(section.sectionName);
        
        LinearLayout llValue = (LinearLayout) circlesRow.findViewById(R.id.llValue);
        
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
        
        TextView tv = new TextView(getActivity());
        
        int tvLeftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        
        tv.setText(Html.fromHtml("Your driver behavior for <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+section.sectionName+"</font> on:"));
        tv.setPadding(tvLeftPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        tv.setTypeface(typeface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        
        final ExpandablePanel panel = (ExpandablePanel) circlesRow.findViewById(R.id.panelExpandable);
        
        final TextView tv2 = new TextView(getActivity());
        final TextView tv3 = new TextView(getActivity());
        
        LinearLayout llCircles = (LinearLayout) circlesRow.findViewById(R.id.llCircles);
        
        DecimalFormat df = new DecimalFormat("0.0");
        
        for (int i = 0; i < section.marks.length; i++) {
            View circleItem = inflater.inflate(R.layout.score_circle_item, llCircles, false);
            int circleBgResId = R.drawable.circle_gray;
            
            String circlesInfo = "";
            
            switch (section.marks[i]) {
			case 0:
				circleBgResId = R.drawable.circle_gray;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#FFFFFF\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">UNDEFINED</font>.";
				break;
			case 1:
				circleBgResId = R.drawable.circle_red;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#f14036\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">NOT GOOD</font>.";
				break;
			case 2:
				circleBgResId = R.drawable.circle_yellow;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#f4bf3d\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">AVERAGE</font>.";
				break;
			case 3:
				circleBgResId = R.drawable.circle_green;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#8da460\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">GOOD</font>.";
				break;

			default:
				break;
			}
            circleItem.findViewById(R.id.circle).setBackgroundResource(circleBgResId);
            ((TextView)circleItem.findViewById(R.id.tvTitle)).setText(circleTitles[i]);
            
            
            final String circleInfo = circlesInfo;
            final String distanceInfo = "We have collected <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+df.format(section.distances[i])+" miles</font> of data for this scoring metric.";
            final int index = i;
            circleItem.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(showTipPopup){
						disableTipPopup();
					}
					
					if(!panel.isExpanded()){
						tv2.setText(Html.fromHtml(circleInfo));
				        tv3.setText(Html.fromHtml(distanceInfo));
						panel.expand();
						rData.arrows[rData.expandedItemIndex].setVisibility(View.INVISIBLE);
						rData.expandedItemIndex = index;
						rData.arrows[rData.expandedItemIndex].setVisibility(View.VISIBLE);
					}
					else if(index==rData.expandedItemIndex){
						panel.setOnExpandListener(null);
						panel.collapse();
					}
					else{
						panel.collapse();
						panel.setOnExpandListener(new OnExpandListener() {
							@Override
							public void onExpand(View handle, View content) {
							}
							
							@Override
							public void onCollapse(View handle, View content) {
								rData.arrows[rData.expandedItemIndex].setVisibility(View.INVISIBLE);
								rData.expandedItemIndex = index;
								rData.arrows[rData.expandedItemIndex].setVisibility(View.VISIBLE);
								tv2.setText(Html.fromHtml(circleInfo));
						        tv3.setText(Html.fromHtml(distanceInfo));
								panel.expand();
							}
						});
					}
				}
			});
            
            llCircles.addView(circleItem);
		}
        rootView.addView(circlesRow);

        tv2.setTypeface(typeface);
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        tv2.setPadding(tvLeftPadding, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        
        tv3.setTypeface(typeface);
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        tv3.setPadding(tvLeftPadding, 0, 0, 0);
        
        LinearLayout llArrows = new LinearLayout(getActivity());
        llArrows.setOrientation(LinearLayout.HORIZONTAL);
	    llArrows.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
	    rData.arrows = new ImageView[circleTitles.length];
	    
	    for (int j = 0; j < circleTitles.length; j++) {
	    	rData.arrows[j] = new ImageView(getActivity());
	    	rData.arrows[j].setImageResource(R.drawable.score_circle_pointer_arrow);
	    	rData.arrows[j].setLayoutParams(new LayoutParams(0,LayoutParams.WRAP_CONTENT, 1));
	    	rData.arrows[j].setVisibility(View.INVISIBLE);
			llArrows.addView(rData.arrows[j]);
		}
        
        llValue.addView(llArrows);
        llValue.addView(tv);
        llValue.addView(tv2);
        llValue.addView(tv3);
        
        return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVED_TITLE, title);
		outState.putSerializable(SAVED_SECTIONS, sections);
	}
	
	class RowData{

		ImageView[] arrows;
		int expandedItemIndex = 0;
		
	}

}
