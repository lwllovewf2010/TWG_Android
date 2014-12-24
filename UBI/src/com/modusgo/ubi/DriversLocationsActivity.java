package com.modusgo.ubi;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class DriversLocationsActivity extends MainActivity {

	ArrayList<Vehicle> drivers;
	
	MapView mapView;
    GoogleMap map;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_map);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Drivers Locations");

		drivers = getVehicles();
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        
        if(map!=null){
	        map.getUiSettings().setMyLocationButtonEnabled(false);
	        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
			        map.animateCamera(cameraUpdate);
			        addVehilesToMap();
				}
			});
	        
	        MapsInitializer.initialize(this);     
        }
	}
	
	private ArrayList<Vehicle> getVehicles(){
		DbHelper dHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dHelper.openDatabase();
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
		
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				Vehicle d = new Vehicle();
				System.out.println("hello");
				
				d.id = c.getLong(0);
				d.name = c.getString(1);
				d.photo = c.getString(2);
				d.markerIcon = c.getString(3);
				d.latitude = c.getDouble(4);
				d.longitude = c.getDouble(5);
				d.address = c.getString(6);
				vehicles.add(d);
				
				c.moveToNext();
			}
		}
		c.close();
		dHelper.closeDatabase();
		dHelper.close();
		return vehicles;
	}
	
	private void addVehilesToMap(){
		Builder builder = LatLngBounds.builder();
		
		for (int i = 0; i < drivers.size(); i++) {
			
			DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
			
	    	final LatLng location = new LatLng(drivers.get(i).latitude, drivers.get(i).longitude);
	    	final String address = drivers.get(i).address;
	    	builder.include(location);
	    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
	    	ImageSize targetSize = new ImageSize(px, px);
	    	ImageLoader.getInstance().loadImage(drivers.get(i).photo, targetSize, options, new ImageLoadingListener() {
				
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
						new CropBitmapTask(location, address).execute(bitmap);
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
		Utils.gaTrackScreen(this, "Drivers Locations Screen");
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
    
    class CropBitmapTask extends AsyncTask<Bitmap, Void, Bitmap>{
    	
    	LatLng location;
    	String address;
    	
    	public CropBitmapTask(LatLng location, String address) {
			this.location = location;
			this.address = address;
		}

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			
			Bitmap mask = BitmapFactory.decodeResource(getResources(), R.drawable.marker_car_mask);
			Bitmap original = Bitmap.createScaledBitmap(params[0], mask.getWidth(), mask.getHeight(), false);
			Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.marker_car);

			Bitmap croppedBitmap = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
			Canvas cropCanvas = new Canvas(croppedBitmap);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			cropCanvas.drawBitmap(original, 0, 0, null);
			cropCanvas.drawBitmap(mask, 0, 0, paint);
			paint.setXfermode(null);
			
			Bitmap result = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(result);
			canvas.drawBitmap(background, 0, 0, null);
			float offset = croppedBitmap.getWidth()*0.12f;
			canvas.drawBitmap(croppedBitmap, offset, offset, null);
			
			return result;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(bitmap!=null){
				map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(bitmap)).title(address));
			}
			super.onPostExecute(bitmap);
		}
    	
    }
}