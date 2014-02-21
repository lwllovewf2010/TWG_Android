package com.modusgo.ubi;

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

	final String LOG_TAG = "myLogs";

	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    Log.d(LOG_TAG, "Fragment1 onCreateView");
	    LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.drivers_fragment, null);
	    
	    int[] backgroundResources = new int[]{R.color.red,R.color.green,R.color.orange,R.color.blue,R.color.yellow,R.color.white};
	    int[] circleSize = new int[]{0,1,0,1};
	    String names[] = new String[]{"Mary", "Kate","John","Philip"};
	    String scores[] = new String[]{"B","A","C","A"};
	    
	    Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    for (int i = 0; i < 4; i++) {
	    	RelativeLayout circleLayout;
	    	
	    	switch(circleSize[i]){
		    	case 0:
		    		circleLayout = (RelativeLayout) inflater.inflate(R.layout.driver_circle_small, null);
			    	break;
		    	case 1:
		    		circleLayout = (RelativeLayout) inflater.inflate(R.layout.driver_circle_big, null);
			    	break;
			    default:
			    	circleLayout = (RelativeLayout) inflater.inflate(R.layout.driver_circle_big, null);
				   	break;
	    	}
	    	//LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,1f);
	    	//p.width = 0;
	    	
		  	//nl.setLayoutParams(p);
	    	Button btn = (Button)circleLayout.findViewById(R.id.button);
	    	btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					startActivity(new Intent(getActivity(),DriverActivity.class));
				}
			});
	    	Drawable btnBack = btn.getBackground();
	    	btnBack.mutate();
	    	btnBack.setColorFilter(getResources().getColor(backgroundResources[i]), PorterDuff.Mode.MULTIPLY);
		    
		    TextView tvName = (TextView)circleLayout.findViewById(R.id.tvName);
		    tvName.setTypeface(robotoLight);
		    tvName.setText(names[i]);
		    
		    TextView tvScore = (TextView)circleLayout.findViewById(R.id.tvScore);
		    tvScore.setTypeface(robotoLight);
		    tvScore.setText(scores[i]);
		    
		    rootView.addView(circleLayout);
		}
	    
	    //Button b = (Button)rootView.findViewById(R.id.button2);
	    //Drawable bg = b.getBackground();
	    //bg.setColorFilter(Color.rgb(255, 0, 0),PorterDuff.Mode.MULTIPLY);
	    return rootView;  
	  }

}
