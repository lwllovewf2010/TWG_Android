package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.farmers.ubi.R;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.modusgo.ubi.Trip.EventType;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.PointContract.PointEntry;
import com.modusgo.ubi.db.RouteContract.RouteEntry;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripActivity extends MainActivity {
	
	public static final String EXTRA_TRIP_ID = "tripId";
	
	long driverId = 0;
	
	MapView mapView;
    GoogleMap map;
    
    long tripId;
    Trip trip;
    
    TextView tvDate;
    TextView tvStartTime;
    TextView tvEndTime;
    TextView tvAvgSpeed;
    TextView tvMaxSpeed;
    TextView tvDistance;
    TextView tvAvgSpeedUnits;
    TextView tvMaxSpeedUnits;
    TextView tvDistanceUnits;
    LinearLayout llTime;
    LinearLayout llContent;
    LinearLayout llEventsList;
    LinearLayout llProgress;
    ScrollView scrollView;
    
    CameraUpdate tripCenterCameraUpdate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_trip);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("TRIP DETAILS");
		
		if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(VehicleEntry._ID);
			tripId = savedInstanceState.getLong(EXTRA_TRIP_ID);
		}
		else if(getIntent()!=null){
			driverId = getIntent().getLongExtra(VehicleEntry._ID,0);
			tripId = getIntent().getLongExtra(EXTRA_TRIP_ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(driverId);
		dHelper.close();
		
		((TextView)findViewById(R.id.tvName)).setText(vehicle.name);
		
		ImageView imagePhoto = (ImageView)findViewById(R.id.imagePhoto);
	    if(vehicle.photo == null || vehicle.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(vehicle.photo, imagePhoto, options);
	    }
	    
		findViewById(R.id.btnSwitchDriverMenu).setVisibility(View.GONE);
		findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		tvDate = (TextView) findViewById(R.id.tvDate);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvDistance = (TextView) findViewById(R.id.tvDistance);
		tvAvgSpeedUnits = (TextView) findViewById(R.id.tvAvgSpeedUnits);
		tvMaxSpeedUnits = (TextView) findViewById(R.id.tvMaxSpeedUnits);
		tvDistanceUnits = (TextView) findViewById(R.id.tvDistanceUnits);
		llTime = (LinearLayout)findViewById(R.id.llTime);
		llContent = (LinearLayout)findViewById(R.id.llContent);
		llEventsList = (LinearLayout)findViewById(R.id.llEventsList);
		llProgress = (LinearLayout)findViewById(R.id.llProgress);
		scrollView = (ScrollView)findViewById(R.id.scrollView);
		
		llTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(map!=null && tripCenterCameraUpdate!=null)
					map.animateCamera(tripCenterCameraUpdate);
			}
		});
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        if(map!=null){
	        map.getUiSettings().setMyLocationButtonEnabled(false);
	
	        MapsInitializer.initialize(this);
	
	        // Updates the location and zoom of the MapView
	        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
	        map.animateCamera(cameraUpdate);
        }
        
        updateActivity();
	
		new GetTripTask(this).execute("vehicles/"+vehicle.id+"/trips/"+tripId+".json");
	}
	
	private Trip getTripFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(TripEntry.TABLE_NAME, 
				new String[]{
				TripEntry._ID,
				TripEntry.COLUMN_NAME_EVENTS_COUNT,
				TripEntry.COLUMN_NAME_START_TIME,
				TripEntry.COLUMN_NAME_END_TIME,
				TripEntry.COLUMN_NAME_DISTANCE,
				TripEntry.COLUMN_NAME_AVG_SPEED,
				TripEntry.COLUMN_NAME_MAX_SPEED,
				TripEntry.COLUMN_NAME_GRADE,
				TripEntry.COLUMN_NAME_VIEWED_AT,
				TripEntry.COLUMN_NAME_UPDATED_AT}, 
				TripEntry._ID+" = ?", new String[]{Long.toString(tripId)}, null, null, null);
		
		Trip t = null;
		
		if(c.moveToFirst()){
			t = new Trip(c.getLong(0), c.getInt(1), c.getString(2), c.getString(3), c.getFloat(4), c.getString(7));
			t.averageSpeed = c.getFloat(5);
			t.maxSpeed = c.getFloat(6);
			t.viewedAt = c.getString(8);
			t.updatedAt = c.getString(9);
		}
		c.close();
		
		if(t!=null){
			c = db.query(RouteEntry.TABLE_NAME, 
					new String[]{
					RouteEntry._ID,
					RouteEntry.COLUMN_NAME_LATITUDE,
					RouteEntry.COLUMN_NAME_LONGITUDE}, 
					RouteEntry.COLUMN_NAME_TRIP_ID+" = ?", new String[]{Long.toString(tripId)}, null, null, RouteEntry._ID+" ASC");
			if(c.moveToFirst()){
				while (!c.isAfterLast()) {
					t.route.add(new LatLng(c.getDouble(1), c.getDouble(2)));
					c.moveToNext();
				}
			}
			c.close();
			
			c = db.query(PointEntry.TABLE_NAME, 
					new String[]{
					PointEntry._ID,
					PointEntry.COLUMN_NAME_LATITUDE,
					PointEntry.COLUMN_NAME_LONGITUDE,
					PointEntry.COLUMN_NAME_EVENT,
					PointEntry.COLUMN_NAME_TITLE,
					PointEntry.COLUMN_NAME_ADDRESS}, 
					PointEntry.COLUMN_NAME_TRIP_ID+" = ?", new String[]{Long.toString(tripId)}, null, null, PointEntry._ID+" ASC");
			if(c.moveToFirst()){
				while (!c.isAfterLast()) {
					EventType event;
					try{
						event = EventType.valueOf(c.getString(3));
					}
					catch(IllegalArgumentException e){
						event = EventType.UNKNOWN;
					}
					
					t.points.add(new Point(new LatLng(c.getDouble(1), c.getDouble(2)), event, c.getString(4), c.getString(5)));
					c.moveToNext();
				}
			}
			c.close();
		}
		
		db.close();
		dbHelper.close();
		
		return t;
	}
	
	private void updateActivity(){
		trip = getTripFromDB();
		
		if(map!=null){
			map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					updateMap();					
				}
			});
		}
		updateLabels();
	}
	
	private void updateLabels(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		
		tvDate.setText(sdfDate.format(trip.getStartDate()));
		tvStartTime.setText(trip.getStartDateString());
		tvEndTime.setText(trip.getEndDateString());
		
		DecimalFormat df = new DecimalFormat("0.0");
		tvAvgSpeed.setText(df.format(trip.averageSpeed));
		tvMaxSpeed.setText(df.format(trip.maxSpeed));
		tvDistance.setText(df.format(trip.distance));
		
		if(prefs.getString(Constants.PREF_UNITS_OF_MEASURE, "mile").equals("mile")){
			tvAvgSpeedUnits.setText("MPH");
			tvMaxSpeedUnits.setText("MPH");
			tvDistanceUnits.setText("MILES");
		}
		else{
			tvAvgSpeedUnits.setText("KPH");
			tvMaxSpeedUnits.setText("KPH");
			tvDistanceUnits.setText("KM");
		}
        
        llEventsList.removeAllViews();
        
        
        for (Point p : trip.points) {
        	if(p.event!=EventType.START && p.event!=EventType.STOP){
	        	RelativeLayout eventItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.trip_event_item, llContent, false);
				((TextView)eventItem.findViewById(R.id.tvTitle)).setText(p.title);
				
				if(!TextUtils.isEmpty(p.address))
					((TextView)eventItem.findViewById(R.id.tvAddress)).setText(p.address);
				else
					((TextView)eventItem.findViewById(R.id.tvAddress)).setText("Address processing in progress");
				
				ImageView icon = (ImageView) eventItem.findViewById(R.id.imageIcon);
				int infoStringResource = 0;
				switch (p.event) {
				case START:
					icon.setImageResource(R.drawable.marker_start);
					break;
				case STOP:
					icon.setImageResource(R.drawable.marker_finish);
					break;
				case HARSH_BRAKING:
					icon.setImageResource(R.drawable.marker_brake);
					infoStringResource = R.string.harsh_braking;
					break;
				case HARSH_ACCELERATION:
					icon.setImageResource(R.drawable.marker_accel);
					infoStringResource = R.string.harsh_accel;
					break;
				case CALL_USAGE:
					icon.setImageResource(R.drawable.marker_phone);
					infoStringResource = R.string.distracted_driving;
					break;
				case PHONE_USAGE:
					icon.setImageResource(R.drawable.marker_app);
					infoStringResource = R.string.distracted_driving;
					break;
				case SPEEDING:
					icon.setImageResource(R.drawable.marker_speeding);
					infoStringResource = R.string.speeding;
					break;
				default:
					icon.setImageResource(R.drawable.marker_unknown);
					break;
				}
				
				if(infoStringResource!=0){
					final int isr = infoStringResource;
					eventItem.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent i = new Intent(TripActivity.this, EducationActivity.class);
							i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, isr);
							startActivity(i);
						}
					});
				}
				else{
					eventItem.findViewById(R.id.imageArrow).setVisibility(View.INVISIBLE);
				}
				
				llEventsList.addView(eventItem);
        	}
		}
	}
	
	private void updateMap(){
		if(map!=null){
			map.clear();
			
			if(trip.route.size()>0){
				PolylineOptions options = new PolylineOptions();
				final Builder builder = LatLngBounds.builder();	
				for (LatLng point : trip.route) {
					options.add(point);
					builder.include(point);
					System.out.println("point "+point.latitude);
				}
		
				int color = Color.parseColor("#009900");
				map.addPolyline(options.color(color).width(8).zIndex(1));
		
				int colorSpeeding = Color.parseColor("#ef4136");
				for (ArrayList<LatLng> route : trip.speedingRoute) {
					PolylineOptions optionsSpeeding = new PolylineOptions();
					for (LatLng point : route) {
						optionsSpeeding.add(point);
					}
					map.addPolyline(optionsSpeeding.color(colorSpeeding).width(8).zIndex(2));
				}
				
				tripCenterCameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 150);
		        map.animateCamera(tripCenterCameraUpdate);
			}
	        
	        for (Point p : trip.points) {
	        	MarkerOptions mo = new MarkerOptions();
	        	mo.position(p.location).title(p.title);
	        	switch (p.event) {
	        	case START:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start)));
	        		break;
	        	case STOP:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_finish)));
	        		break;
	        	case HARSH_BRAKING:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_brake)));
	        		break;
	        	case HARSH_ACCELERATION:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_accel)));
	        		break;
	        	case CALL_USAGE:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_phone)));
	        		break;
	        	case PHONE_USAGE:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_app)));
	        		break;
	        	case SPEEDING:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_speeding)));
	        		break;
	        	case UNKNOWN:
	        		map.addMarker(mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_unknown)));
	        		break;
	        	default:
	        		break;
	        	}
	        }	        	
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(VehicleEntry._ID, driverId);
		outState.putLong(EXTRA_TRIP_ID, tripId);
		super.onSaveInstanceState(outState);
	}
	
	@Override
    public void onResume() {
        mapView.onResume();
        Utils.gaTrackScreen(this, "Trip Screen");
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
    
    class GetTripTask extends BaseRequestAsyncTask{
		
		public GetTripTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("trip_id", ""+tripId));
	        requestParams.add(new BasicNameValuePair("vehicle_id", ""+vehicle.id));
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			Trip trip = new Trip(tripId, responseJSON.optInt("harsh_events_count"), Utils.fixTimezoneZ(responseJSON.optString("start_time")), Utils.fixTimezoneZ(responseJSON.optString("end_time")), responseJSON.optDouble("mileage"), responseJSON.optString("grade"));
			
			trip.averageSpeed = responseJSON.optDouble("avg_speed");
			trip.maxSpeed = responseJSON.optDouble("max_speed");
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			trip.viewed = true;
			trip.updatedAt = responseJSON.optString("updated_at");
			trip.viewedAt = sdf.format(Calendar.getInstance().getTime());
			trip.fuelLevel = responseJSON.optInt("fuel_left");
			trip.fuelUnit = responseJSON.optString("fuel_unit");
			
			if(responseJSON.has("route")){
				JSONArray routeJSON = responseJSON.getJSONArray("route");
				for (int i = 0; i < routeJSON.length(); i++) {
					JSONArray pointJSON = routeJSON.getJSONArray(i);
					trip.route.add(new LatLng(pointJSON.optDouble(0), pointJSON.optDouble(1)));					
				}
			}
			
			if(responseJSON.has("speeding")){
				JSONArray speedingJSON = responseJSON.getJSONArray("speeding");
				for (int i = 0; i < speedingJSON.length(); i++) {
					JSONArray speedingRouteJSON = speedingJSON.getJSONObject(i).getJSONArray("route");
					ArrayList<LatLng> speedingRoute = new ArrayList<LatLng>();
					
					for (int j = 0; j < speedingRouteJSON.length(); j++) {
						JSONArray pointJSON = speedingRouteJSON.getJSONArray(j);
						speedingRoute.add(new LatLng(pointJSON.optDouble(0,0), pointJSON.optDouble(1,0)));	
					}
					trip.speedingRoute.add(speedingRoute);				
				}
			}
			
			if(responseJSON.has("points")){
				JSONArray pointsJSON = responseJSON.getJSONArray("points");
				for (int i = 0; i < pointsJSON.length(); i++) {
					JSONObject pointJSON = pointsJSON.getJSONObject(i);
					
					if(pointJSON.has("location")){
						JSONObject locationJSON = pointJSON.getJSONObject("location");
						Point p = new Point(new LatLng(locationJSON.optDouble("latitude",0), locationJSON.optDouble("longitude",0)),
								getEventType(pointJSON.optString("event")),
								pointJSON.optString("title"), "");
						p.fetchAddress(context.getApplicationContext());
						trip.points.add(p);
					}
				}
			}
			
			DbHelper dHelper = DbHelper.getInstance(TripActivity.this);
			dHelper.saveTrip(vehicle.id, trip);
			dHelper.saveRoute(trip.id, trip.route);
			dHelper.savePoints(trip.id, trip.points);
			dHelper.close();			
			
			updateActivity();
			
			super.onSuccess(responseJSON);
		}
		
		private EventType getEventType(String event){
			switch (event) {
			case "start":
				return EventType.START;
			case "stop":
				return EventType.STOP;
			case "harsh_braking":
				return EventType.HARSH_BRAKING;
			case "harsh_acceleration":
				return EventType.HARSH_ACCELERATION;
			case "call_usage":
				return EventType.CALL_USAGE;
			case "phone_usage":
				return EventType.PHONE_USAGE;
			case "speeding":
				return EventType.SPEEDING;
			default:
				return EventType.UNKNOWN;
			}
		}
	}
}