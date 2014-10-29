package com.modusgo.ubi;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;

public class MapActivity extends MainActivity {

	Driver driver;
	long driverId = 0;
	
	MapView mapView;
    GoogleMap map;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_map);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Current Location");
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		driver = dHelper.getDriverShort(driverId);
		dHelper.close();
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 14);
		        map.animateCamera(cameraUpdate);
		        addDriverToMap();
			}
		});
        
        MapsInitializer.initialize(this);     

	}
	
	private void addDriverToMap(){
		LatLng location = new LatLng(driver.latitude, driver.longitude);
		map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)).title(driver.address));
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
        map.animateCamera(cameraUpdate);
	}
	
	@Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}