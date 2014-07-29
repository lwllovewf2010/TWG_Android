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
		
		Driver d = getDriver(getArguments().getInt("id", 0));

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
    
    //TODO : get driver from db
    Driver getDriver(int id) {
    	ArrayList<Driver> data = new ArrayList<Driver>();
		data.add(new Driver("Melissa Hasalonglastname", R.drawable.person_test, "2012 Ford Edge","07/05/2014 05:00 PM PST", true, true));
		data.add(new Driver("Diana Johnson", R.drawable.person_test2, "2011 Ford Focus","07/05/2014 05:00 PM PST", true, false));
		data.add(new Driver("Kate Summerton", R.drawable.person_test3, "1967 Ford Mustang","07/05/2014 05:00 PM PST", false, true));
		
		return (data.get(id));
	}
}