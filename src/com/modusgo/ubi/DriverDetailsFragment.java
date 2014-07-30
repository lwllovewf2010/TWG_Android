package com.modusgo.ubi;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
	    
	    if(d.alertsOK){
	    	((ImageView)rootView.findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_green);
	    }else{
	    	((ImageView)rootView.findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_red);		    	
	    }
		
		return rootView;
	}
}