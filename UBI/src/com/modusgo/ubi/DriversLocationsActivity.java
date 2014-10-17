package com.modusgo.ubi;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.demo.R;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class DriversLocationsActivity extends MainActivity {

	ArrayList<Driver> drivers;
	
	MapView mapView;
    GoogleMap map;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_map);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Drivers Locations");

		drivers = getDrivers();
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
		        map.animateCamera(cameraUpdate);
		        addDriversToMap();
			}
		});
//        map.setOnMarkerClickListener(new OnMarkerClickListener() {
//			@Override
//			public boolean onMarkerClick(Marker marker) {
//				for (int i = 0; i < drivers.size(); i++) {
//					if(drivers.get(i).name.equals(marker.getTitle())){
//						Intent intent = new Intent(MapActivity.this, DriverActivity.class);
//						intent.putExtra("id", i);
//						startActivity(intent);
//						break;
//					}
//				}
//				
//				return false;
//			}
//		});
        
        MapsInitializer.initialize(this);     

	}
	
	private ArrayList<Driver> getDrivers(){
		DbHelper dHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dHelper.getReadableDatabase();
		Cursor c = db.query(VehicleEntry.TABLE_NAME, 
				new String[]{
				VehicleEntry._ID,
				VehicleEntry.COLUMN_NAME_DRIVER_NAME,
				VehicleEntry.COLUMN_NAME_DRIVER_PHOTO,
				VehicleEntry.COLUMN_NAME_DRIVER_MARKER_ICON,
				VehicleEntry.COLUMN_NAME_LATITUDE,
				VehicleEntry.COLUMN_NAME_LONGITUDE,
				VehicleEntry.COLUMN_NAME_ADDRESS}, 
				null, null, null, null, null);
		
		ArrayList<Driver> drivers = new ArrayList<Driver>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				Driver d = new Driver();
				System.out.println("hello");
				
				d.id = c.getLong(0);
				d.name = c.getString(1);
				d.photo = c.getString(2);
				d.markerIcon = c.getString(3);
				d.latitude = c.getDouble(4);
				d.longitude = c.getDouble(5);
				d.address = c.getString(6);
				drivers.add(d);
				
				c.moveToNext();
			}
		}
		c.close();
		db.close();
		dHelper.close();
		return drivers;
	}
	
	private void addDriversToMap(){
		Builder builder = LatLngBounds.builder();
		
		for (int i = 0; i < drivers.size(); i++) {
			
			DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
			
	    	final LatLng location = new LatLng(drivers.get(i).latitude, drivers.get(i).longitude);
	    	final String address = drivers.get(i).address;
	    	builder.include(location);
	    	ImageLoader.getInstance().loadImage(drivers.get(i).markerIcon, options, new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String arg0, View arg1) {					
				}
				
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)).title(address));
				}
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap) {
					if(bitmap!=null)
						map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).title(address));
					else
						map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)).title(address));
				}
				
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {					
				}
			});
		}
		
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 150);
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