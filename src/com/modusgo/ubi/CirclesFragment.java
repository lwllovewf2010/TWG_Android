package com.modusgo.ubi;

import java.util.Random;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.modusgo.ubi.customviews.ExpandablePanel;
import com.modusgo.ubi.customviews.ExpandablePanel.OnExpandListener;

public class CirclesFragment extends TitledFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(savedInstanceState!=null){
			title = savedInstanceState.getString(SAVED_TITLE);
	    }
	    else if(getArguments()!=null){  
			title = getArguments().getString(SAVED_TITLE);  
	    }
		
		LinearLayout rootView = new LinearLayout(getActivity());
		rootView.setOrientation(LinearLayout.VERTICAL);
	    LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	    rootView.setLayoutParams(rootParams);
		
	    rootView.addView(createNewRow("Use of Speed", getRandomMarks(), inflater));
	    rootView.addView(createNewRow("Cornering", getRandomMarks(), inflater));
	    rootView.addView(createNewRow("Intersection Acceleration", getRandomMarks(), inflater));
	    rootView.addView(createNewRow("Road Acceleration", getRandomMarks(), inflater));
	    rootView.addView(createNewRow("Intersection Braking", getRandomMarks(), inflater));
	    rootView.addView(createNewRow("Road Braking", getRandomMarks(), inflater));		
		
		return rootView;
	}
	
	private int[] getRandomMarks(){
		Random r = new Random();
		return new int[]{r.nextInt(4),r.nextInt(4),r.nextInt(4),r.nextInt(4)};
	}
	
	private View createNewRow(String rowTitle, int[] marks, LayoutInflater inflater){
		LinearLayout rootView = new LinearLayout(getActivity());
		rootView.setOrientation(LinearLayout.VERTICAL);
	    LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	    rootView.setLayoutParams(rootParams);
	    
	    final RowData rData = new RowData();
		
		String[] circleTitles = new String[]{"Highway", "Major Road", "Minor Road", "Local Road"};
	    
		LinearLayout circlesRow = (LinearLayout) inflater.inflate(R.layout.score_circles_row_item, rootView, false);
        ((TextView)circlesRow.findViewById(R.id.tvTitle)).setText(rowTitle);
        
        LinearLayout llValue = (LinearLayout) circlesRow.findViewById(R.id.llValue);
        
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
        
        TextView tv = new TextView(getActivity());
        
        int tvLeftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        
        tv.setText(Html.fromHtml("Your driver behavior for <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+rowTitle+"</font> on:"));
        tv.setPadding(tvLeftPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        tv.setTypeface(typeface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        
        final ExpandablePanel panel = (ExpandablePanel) circlesRow.findViewById(R.id.panelExpandable);
        
        final TextView tv2 = new TextView(getActivity());
        
        LinearLayout llCircles = (LinearLayout) circlesRow.findViewById(R.id.llCircles);
        for (int i = 0; i < marks.length; i++) {
            LinearLayout circleItem = (LinearLayout) inflater.inflate(R.layout.score_circle_item, llCircles, false);
            int circleBgResId = R.drawable.circle_gray;
            
            String circlesInfo = "";
            
            switch (marks[i]) {
			case 0:
				circleBgResId = R.drawable.circle_gray;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#FFFFFF\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">UNDEFINED</font>.";
				break;
			case 1:
				circleBgResId = R.drawable.circle_red;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#EF4036\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">NOT GOOD</font>.";
				break;
			case 2:
				circleBgResId = R.drawable.circle_yellow;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#FBC740\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">AVERAGE</font>.";
				break;
			case 3:
				circleBgResId = R.drawable.circle_green;
				circlesInfo+="<font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+circleTitles[i]+"</font> in an <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+title+"</font> area is <font color=\"#A5CE41\" face=\"fonts/EncodeSansNormal-500-Medium.ttf\">GOOD</font>.";
				break;

			default:
				break;
			}
            circleItem.findViewById(R.id.circle).setBackgroundResource(circleBgResId);
            ((TextView)circleItem.findViewById(R.id.tvTitle)).setText(circleTitles[i]);
            
            
            final String circleInfo = circlesInfo;
            final int index = i;
            circleItem.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!panel.isExpanded()){
						tv2.setText(Html.fromHtml(circleInfo));
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
								panel.expand();
							}
						});
					}
				}
			});
            
            llCircles.addView(circleItem);
		}
        rootView.addView(circlesRow);
       
        TextView tv3 = new TextView(getActivity());
        tv3.setText(Html.fromHtml("We have collected <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">500 miles</font> of data for this scoring metric."));

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
	}
	
	class RowData{

		ImageView[] arrows;
		int expandedItemIndex = 0;
		
	}

}
