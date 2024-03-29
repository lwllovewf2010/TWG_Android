package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import com.modusgo.ubi.db.SpeedingRouteContract.SpeedingRouteEntry;
import com.modusgo.ubi.db.TripContract.TripEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripActivity extends MainActivity {

	public static final String EXTRA_VEHICLE_ID = VehicleEntry._ID;
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
    TextView tvScore;
    ImageView imageArrow;
    TextView tvFuelUsed;
    TextView tvFuelCost;
    TextView tvFuelUnits;
    TextView tvFuelStatus;
    LinearLayout llFuelUsed;
    LinearLayout llFuelCost;
    LinearLayout llTime;
    LinearLayout llContent;
    LinearLayout llEventsList;
    LinearLayout llProgress;
    ScrollView scrollView;
    
    CameraUpdate tripCenterCameraUpdate;
    boolean mapCentered = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_trip);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("TRIP DETAILS");
		
		
		if(getIntent()!=null){
			driverId = getIntent().getLongExtra(EXTRA_VEHICLE_ID,0);
			tripId = getIntent().getLongExtra(EXTRA_TRIP_ID,0);
		}
		else if(savedInstanceState!=null){
			driverId = savedInstanceState.getLong(EXTRA_VEHICLE_ID);
			tripId = savedInstanceState.getLong(EXTRA_TRIP_ID);
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
		findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
		

		tvDate = (TextView) findViewById(R.id.tvDate);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvDistance = (TextView) findViewById(R.id.tvDistance);
		tvAvgSpeedUnits = (TextView) findViewById(R.id.tvAvgSpeedUnits);
		tvMaxSpeedUnits = (TextView) findViewById(R.id.tvMaxSpeedUnits);
		tvDistanceUnits = (TextView) findViewById(R.id.tvDistanceUnits);
	    tvScore = (TextView) findViewById(R.id.tvScore);
	    imageArrow = (ImageView) findViewById(R.id.imageArrow);
	    tvFuelUsed = (TextView) findViewById(R.id.tvFuelUsed);
	    tvFuelCost = (TextView) findViewById(R.id.tvFuelCost);
	    tvFuelUnits = (TextView) findViewById(R.id.tvFuelUnits);
	    tvFuelStatus = (TextView) findViewById(R.id.tvFuelStatus);
	    llFuelUsed = (LinearLayout)findViewById(R.id.llFuelUsed);
	    llFuelCost = (LinearLayout)findViewById(R.id.llFuelCost);
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
        
        imageArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        imageArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(TripActivity.this, "Your per trip score is being calculated.", Toast.LENGTH_SHORT).show();
			}
		});
        
        updateActivity();

		new GetTripTask(this).execute("vehicles/"+vehicle.id+"/trips/"+tripId+".json");
        
	}
	
	private Trip getTripFromDB(){
		DbHelper dbHelper = DbHelper.getInstance(this);
		SQLiteDatabase db = dbHelper.openDatabase();
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
				TripEntry.COLUMN_NAME_FUEL,
				TripEntry.COLUMN_NAME_FUEL_UNIT,
				TripEntry.COLUMN_NAME_FUEL_STATUS,
				TripEntry.COLUMN_NAME_FUEL_COST,
				TripEntry.COLUMN_NAME_VIEWED_AT,
				TripEntry.COLUMN_NAME_UPDATED_AT}, 
				TripEntry._ID+" = ?", new String[]{Long.toString(tripId)}, null, null, null);
		
		Trip t = null;
		
		if(c.moveToFirst()){
			t = new Trip(prefs, c.getLong(0), c.getInt(1), c.getString(2), c.getString(3), c.getFloat(4), c.getString(7));
			t.averageSpeed = c.getFloat(5);
			t.maxSpeed = c.getFloat(6);
			t.fuel = c.getFloat(8);
			t.fuelUnit = c.getString(9);
			t.fuelStatus = c.getString(10);
			t.fuelCost = c.getFloat(11);
			t.viewedAt = c.getString(12);
			t.updatedAt = c.getString(13);
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
			
			c = db.query(SpeedingRouteEntry.TABLE_NAME, 
					new String[]{
					SpeedingRouteEntry._ID,
					SpeedingRouteEntry.COLUMN_NAME_NUM,
					SpeedingRouteEntry.COLUMN_NAME_LATITUDE,
					SpeedingRouteEntry.COLUMN_NAME_LONGITUDE}, 
					SpeedingRouteEntry.COLUMN_NAME_TRIP_ID+" = ?", new String[]{Long.toString(tripId)}, null, null, SpeedingRouteEntry._ID+" ASC");
			if(c.moveToFirst()){

			    System.out.println("Loading speeding, "+c.getCount());
				int lastNum = 0;
				ArrayList<LatLng> speedingRoute = new ArrayList<LatLng>();
				while (!c.isAfterLast()) {
					if(lastNum!=c.getInt(1)){
						t.speedingRoutes.add(speedingRoute);
						speedingRoute = new ArrayList<LatLng>();
					}
					speedingRoute.add(new LatLng(c.getDouble(2), c.getDouble(3)));
					lastNum = c.getInt(1);
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

		dbHelper.closeDatabase();
		dbHelper.close();
		
		return t;
	}
	
	private void updateActivity(){
		trip = getTripFromDB();
		
		if(trip!=null){
			if(map!=null){
				map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
					@Override
					public void onMapLoaded() {
						updateMap();
					}
				});
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE));
	        
	        try{
		        Calendar cViewedAt = Calendar.getInstance();
		        cViewedAt.setTimeZone(TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE));
		        cViewedAt.setTime(sdf.parse(trip.viewedAt));
		        
		        long timeDifference = System.currentTimeMillis() - cViewedAt.getTimeInMillis();
		        if(timeDifference<5000)
		    		new GetTripTask(this).execute("vehicles/"+vehicle.id+"/trips/"+tripId+".json");
	        }
	        catch(ParseException e){
	        	e.printStackTrace();
	        }
			
			updateLabels();
		}
	}
	
	private void updateLabels(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		TimeZone tzTo = TimeZone.getTimeZone(prefs.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfDate.setTimeZone(tzTo);
		
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
		
		if(trip.grade.contains("A") || 
				trip.grade.contains("B") || 
				trip.grade.contains("C") || 
				trip.grade.contains("D") || 
				trip.grade.contains("E") || 
				trip.grade.contains("F")){
			tvScore.setText(trip.grade);
			imageArrow.setVisibility(View.GONE);
			tvScore.setVisibility(View.VISIBLE);
		}
		else{
			imageArrow.setVisibility(View.VISIBLE);
			tvScore.setVisibility(View.GONE);			
		}
		
		if(trip.fuelCost==0)
			llFuelCost.setVisibility(View.GONE);
		else{
			DecimalFormat moneyDf = new DecimalFormat("0.00");
			tvFuelCost.setText(moneyDf.format(trip.fuelCost));
		}
		
		System.out.println("fuel: "+trip.fuel+" unit: "+trip.fuelUnit);
		if(trip.fuel >= 0 && !TextUtils.isEmpty(trip.fuelUnit)){
			if(trip.fuelUnit.equals("%")){
				String fuelString = df.format(trip.fuel)+trip.fuelUnit;
				int fuelUnitLength = trip.fuelUnit.length();
			    SpannableStringBuilder cs = new SpannableStringBuilder(fuelString);
			    cs.setSpan(new SuperscriptSpan(), fuelString.length()-fuelUnitLength, fuelString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			    cs.setSpan(new RelativeSizeSpan(0.6f), fuelString.length()-fuelUnitLength, fuelString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				tvFuelUsed.setText(cs);
				tvFuelUnits.setText("");
			}
			else{
				tvFuelUsed.setText(df.format(trip.fuel));
				tvFuelUnits.setText(trip.fuelUnit);
			}
		}
		else{
			llFuelUsed.setVisibility(View.GONE);
			if(!TextUtils.isEmpty(trip.fuelStatus)){
				tvFuelStatus.setVisibility(View.VISIBLE);
				tvFuelStatus.setText(trip.fuelStatus);
			}
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
				for (ArrayList<LatLng> route : trip.speedingRoutes) {
					PolylineOptions optionsSpeeding = new PolylineOptions();
					for (LatLng point : route) {
						optionsSpeeding.add(point);
					}
					map.addPolyline(optionsSpeeding.color(colorSpeeding).width(8).zIndex(2));
				}
				
				if(!mapCentered){
					try{
						int mapPadding = (int) Math.min(mapView.getHeight()*0.2f, mapView.getWidth()*0.2f);
						tripCenterCameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), mapPadding);
				        map.animateCamera(tripCenterCameraUpdate);
					}
					catch(IllegalStateException e){
						tripCenterCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(trip.route.get(trip.route.size()/2), 10, 0, 0));
				        map.animateCamera(tripCenterCameraUpdate);
					}
					mapCentered = true;
				}
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
		outState.putLong(EXTRA_VEHICLE_ID, driverId);
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
	protected void onPause() {
		super.onPause();
		mapView.onPause();
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
			Trip trip = new Trip(prefs, tripId, responseJSON.optInt("harsh_events_count"), Utils.fixTimezoneZ(responseJSON.optString("start_time")), Utils.fixTimezoneZ(responseJSON.optString("end_time")), responseJSON.optDouble("mileage"), responseJSON.optString("grade"));
			
			trip.averageSpeed = responseJSON.optDouble("avg_speed");
			trip.maxSpeed = responseJSON.optDouble("max_speed");
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE));
			trip.viewed = true;
			trip.updatedAt = responseJSON.optString("updated_at");
			trip.viewedAt = sdf.format(Calendar.getInstance().getTime());
			trip.fuel = (float) responseJSON.optDouble("fuel_used");
			trip.fuelUnit = responseJSON.optString("fuel_unit");
			trip.fuelCost = (float) responseJSON.optDouble("fuel_cost");
			trip.fuelStatus = responseJSON.optString("fuel_status");
			
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
					trip.speedingRoutes.add(speedingRoute);				
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
						trip.points.add(p);
					}
				}
			}
			
			DbHelper dHelper = DbHelper.getInstance(TripActivity.this);
			dHelper.saveTrip(vehicle.id, trip);
			dHelper.saveRoute(trip.id, trip.route);
			dHelper.saveSpeedingRoute(trip.id, trip.speedingRoutes);
			dHelper.savePoints(trip.id, trip.points);
			dHelper.close();
			
			updateActivity();

        	new FetchAddresses(context).execute();
			
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
    
    class FetchAddresses extends AsyncTask<Void, Void, Void> {
    	
    	Context context;
    	
    	public FetchAddresses(Context context) {
    		this.context = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			boolean needSavePoints = false;
			
			for (Point p : trip.points) {
				if(TextUtils.isEmpty(p.address)){
					needSavePoints = true;
					p.fetchAddress(context.getApplicationContext());
				}
			}
			
			if(needSavePoints){
				DbHelper dHelper = DbHelper.getInstance(TripActivity.this);
				dHelper.savePoints(trip.id, trip.points);
				dHelper.close();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			updateLabels();
			super.onPostExecute(result);
		}
    }
}