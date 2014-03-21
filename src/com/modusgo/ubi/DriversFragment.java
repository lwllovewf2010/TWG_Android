package com.modusgo.ubi;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DriversFragment extends Fragment {

	private final static String SAVED_DRIVERS = "drivers";
	
	final String LOG_TAG = "myLogs";
	ArrayList<Driver> drivers;
	
	public DriversFragment(){
	}
	
	public DriversFragment(ArrayList<Driver> drivers){
		this.drivers = drivers;
	}

	@SuppressWarnings("unchecked")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    Log.d(LOG_TAG, "Fragment1 onCreateView");
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.drivers_fragment, null);
	    
	    if(savedInstanceState!=null) {
            drivers = (ArrayList<Driver>) savedInstanceState.getSerializable(SAVED_DRIVERS);
        }
	    
	    int[] backgroundResources = new int[]{R.color.red,R.color.green,R.color.orange,R.color.blue,R.color.yellow,R.color.white};

	    Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    for (int i = 0; i < drivers.size(); i++) {
	    	RelativeLayout circleLayout;
	    	
	    	if(drivers.get(i).score.equals("A")){
	    		circleLayout = (RelativeLayout) inflater.inflate(R.layout.driver_circle_big, null);	    		
	    	}
	    	else{
	    		circleLayout = (RelativeLayout) inflater.inflate(R.layout.driver_circle_small, null);	    		
	    	}
	    	//LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,1f);
	    	//p.width = 0;
	    	
		  	//nl.setLayoutParams(p);
	    	Button btn = (Button)circleLayout.findViewById(R.id.button);
	    	btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					getActivity().getSupportFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
					.replace(R.id.content_frame, new DriverFragment())
					.addToBackStack(null)
					.commit();
					//startActivity(new Intent(getActivity(),DriverFragment.class));
				}
			});
	    	Drawable btnBack = btn.getBackground();
	    	btnBack.mutate();
	    	btnBack.setColorFilter(getResources().getColor(backgroundResources[i]), PorterDuff.Mode.MULTIPLY);
		    
		    TextView tvName = (TextView)circleLayout.findViewById(R.id.tvName);
		    tvName.setTypeface(robotoLight);
		    tvName.setText(drivers.get(i).name);
		    
		    TextView tvScore = (TextView)circleLayout.findViewById(R.id.tvScore);
		    tvScore.setTypeface(robotoLight);
		    tvScore.setText(drivers.get(i).score);
		    
		    rootView.addView(circleLayout);
		}
	    
	    //Button b = (Button)rootView.findViewById(R.id.button2);
	    //Drawable bg = b.getBackground();
	    //bg.setColorFilter(Color.rgb(255, 0, 0),PorterDuff.Mode.MULTIPLY);
	    return rootView;  
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SAVED_DRIVERS, drivers);
	}

}
