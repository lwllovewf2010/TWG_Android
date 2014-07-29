package com.modusgo.ubi;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DriverDetailsActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_info_fragment);
        
        Driver d = getDriver(getIntent().getIntExtra("id", 0));

	    ((TextView) findViewById(R.id.tvName)).setText(d.name);
	    ((TextView) findViewById(R.id.tvVehicle)).setText(d.vehicle);
	    ((TextView) findViewById(R.id.tvDate)).setText(d.lastTripDate);
	    
	    ((ImageView)findViewById(R.id.imagePhoto)).setImageResource(d.imageId);
	    
	    if(d.diagnosticsOK){
	    	((ImageView)findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_green);
	    }else{
	    	((ImageView)findViewById(R.id.imageDiagnostics)).setImageResource(R.drawable.ic_diagnostics_red);		    	
	    }
	    
	    if(d.alertsOK){
	    	((ImageView)findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_green);
	    }else{
	    	((ImageView)findViewById(R.id.imageAlerts)).setImageResource(R.drawable.ic_alerts_red);		    	
	    }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();  
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