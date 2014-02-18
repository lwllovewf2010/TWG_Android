package com.modusgo.ubi;

import com.modusgo.modusadmin.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ChartFragment extends Fragment {

	private final static String SAVED_PERCENTS = "columnPercents";
	final String LOG_TAG = "myLogs";
	float[] columnPercents;
	
	View[] columns;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    Log.d(LOG_TAG, "Fragment2 onCreateView");
	    LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.chart_fragment, null);
	    //View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false); 
        Bundle args = getArguments();
        //((TextView) rootView.findViewById(android.R.id.text1)).setText(Integer.toString(args.getInt(ARG_OBJECT)));

	    int childcount = ll.getChildCount();
	    if(savedInstanceState != null) {
	        columnPercents = savedInstanceState.getFloatArray(SAVED_PERCENTS);
	    }
	    
	    columns = new View[childcount];
	    for (int i=0; i < childcount; i++){
	          columns[i] = ll.getChildAt(i).findViewById(R.id.view);
	    }	    
	    
	    return  ll;
	}
	  
	public void setColumnsHeight(float[] heightPercents){
		columnPercents = heightPercents;
		if(columns!=null){
			for (int i = 0; i < columnPercents.length; i++) {
				LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) columns[i].getLayoutParams();
			  	p.weight = columnPercents[i];
			  	columns[i].setLayoutParams(p);
			}
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		setColumnsHeight(columnPercents);
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    outState.putFloatArray(SAVED_PERCENTS, columnPercents);
	}
}
