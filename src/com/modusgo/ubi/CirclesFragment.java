package com.modusgo.ubi;

import java.util.Random;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

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
		
		String[] circleTitles = new String[]{"Highway", "Major Road", "Minor Road", "Local Road"};
	    
		LinearLayout circlesRow = (LinearLayout) inflater.inflate(R.layout.score_circles_row_item, rootView, false);
        ((TextView)circlesRow.findViewById(R.id.tvTitle)).setText(rowTitle);
        
        LinearLayout llValue = (LinearLayout) circlesRow.findViewById(R.id.llValue);
        
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
        
        TextView tv = new TextView(getActivity());
        tv.setText(Html.fromHtml("Your driver behavior for <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">"+rowTitle+"</font> on:"));
        tv.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        tv.setTypeface(typeface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        
        String circlesInfo = "the "; 
        
        LinearLayout llCircles = (LinearLayout) circlesRow.findViewById(R.id.llCircles);
        for (int i = 0; i < marks.length; i++) {
            LinearLayout circleItem = (LinearLayout) inflater.inflate(R.layout.score_circle_item, llCircles, false);
            int circleBgResId = R.drawable.circle_gray;
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
            if(i!=marks.length-1)
            	circlesInfo+="<br>";
            circleItem.findViewById(R.id.circle).setBackgroundResource(circleBgResId);
            ((TextView)circleItem.findViewById(R.id.tvTitle)).setText(circleTitles[i]);
            llCircles.addView(circleItem);
		}
        rootView.addView(circlesRow);
        
        TextView tv2 = new TextView(getActivity());
        tv2.setText(Html.fromHtml(circlesInfo));
       
        
        TextView tv3 = new TextView(getActivity());
        tv3.setText(Html.fromHtml("We have collected <font face=\"fonts/EncodeSansNormal-500-Medium.ttf\">500 miles</font> of data for this scoring metric."));

        tv2.setTypeface(typeface);
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        tv2.setPadding(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        
        tv3.setTypeface(typeface);
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8.5f);
        llValue.addView(tv);
        llValue.addView(tv2);
        llValue.addView(tv3);
        
        return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVED_TITLE, title);
	}

}
