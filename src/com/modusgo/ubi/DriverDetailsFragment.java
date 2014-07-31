package com.modusgo.ubi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DriverDetailsFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.driver_info_fragment, null);

		((MainActivity)getActivity()).setActionBarTitle("DRIVER DETAIL");
		
		Driver d = DbHelper.getDrivers().get(getArguments().getInt("id", 0));

	    ((TextView) rootView.findViewById(R.id.tvName)).setText(d.name);
	    ((TextView) rootView.findViewById(R.id.tvVehicle)).setText(d.vehicle);
	    ((TextView) rootView.findViewById(R.id.tvDate)).setText(d.lastTripDate);
	    
	    ((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(d.imageId);
	    
	    if(d.diagnosticsOK){
	    	((ImageView)rootView.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_green);
	    }else{
	    	((ImageView)rootView.findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_red);		    	
	    }
	    
	    ImageView imageAlerts = (ImageView)rootView.findViewById(R.id.imageAlerts);
	    
	    if(d.alertsOK){
	    	imageAlerts.setImageResource(R.drawable.ic_alerts_green);
	    }else{
	    	imageAlerts.setImageResource(R.drawable.ic_alerts_red);		    	
	    }
	    
	    imageAlerts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AlertsActivity.class));			}
		});
		
		return rootView;
	}
}