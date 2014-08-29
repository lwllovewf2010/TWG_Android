package com.modusgo.ubi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.modusgo.ubi.Trip.Event;
import com.modusgo.ubi.Trip.EventType;
import com.modusgo.ubi.Trip.Point;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TripActivity extends MainActivity {
	
	public static final String EXTRA_TRIP_ID = "tripId";
	
	Driver driver;
	DriversHelper dHelper;
	int driverIndex = 0;
	
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
    LinearLayout llContent;
    LinearLayout llEventsList;
    LinearLayout llProgress;
    ScrollView scrollView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_trip);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("TRIP DETAILS");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
			tripId = savedInstanceState.getLong(EXTRA_TRIP_ID);
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
			tripId = getIntent().getLongExtra(EXTRA_TRIP_ID,0);
		}

		dHelper = DriversHelper.getInstance();
		driver = dHelper.getDriverByIndex(driverIndex);
		
		((TextView)findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)findViewById(R.id.imagePhoto);
	    if(driver.imageUrl == null || driver.imageUrl.equals(""))
	    	imagePhoto.setImageResource(driver.imageId);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.imageUrl, imagePhoto, options);
	    }
	    
		findViewById(R.id.btnSwitchDriverMenu).setVisibility(View.GONE);
		findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		tvDate = (TextView) findViewById(R.id.tvDate);
		tvStartTime = (TextView) findViewById(R.id.tvStartTime);
		tvEndTime = (TextView) findViewById(R.id.tvEndTime);
		tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvDistance = (TextView) findViewById(R.id.tvDistance);
		llContent = (LinearLayout)findViewById(R.id.llContent);
		llEventsList = (LinearLayout)findViewById(R.id.llEventsList);
		llProgress = (LinearLayout)findViewById(R.id.llProgress);
		scrollView = (ScrollView)findViewById(R.id.scrollView);		
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);

        MapsInitializer.initialize(this);

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(driver.latitude, driver.longitude), 10);
        map.animateCamera(cameraUpdate);
        
		new GetTripTask(this).execute("drivers/"+driver.id+"/trips/"+tripId+".json");
	}
	
	private void updateActivity(){
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
		
		tvDate.setText(sdfDate.format(trip.getStartDate()));
		tvStartTime.setText(trip.getStartDateString());
		tvEndTime.setText(trip.getEndDateString());
		
		DecimalFormat df = new DecimalFormat("0.0");
		tvAvgSpeed.setText(df.format(trip.averageSpeed));
		tvMaxSpeed.setText(df.format(trip.maxSpeed));
		tvDistance.setText(df.format(trip.distance));
		
		map.clear();
		
		PolylineOptions options = new PolylineOptions();
		
		final Builder builder = LatLngBounds.builder();
		
		for (LatLng point : trip.route) {
			options.add(point);
			builder.include(point);
		}

		int color = Color.parseColor("#009900");
		map.addPolyline(options.color(color).width(8));
		
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 150);
        map.animateCamera(cameraUpdate);
        
        for (Point p : trip.points) {
        	for (EventType e : p.events) {
				switch (e) {
				case START:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_start)));
					break;
				case STOP:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_finish)));
					break;
				case HARSH_BRAKING:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_brake)));
					break;
				case HARSH_ACCELERATION:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_accel)));
					break;
				case PHONE_USAGE:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_phone)));
					break;
				case APP_USAGE:
					map.addMarker(new MarkerOptions().position(p.location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_app)));
					break;
				default:
					break;
				}
			}
		}
        
        llEventsList.removeAllViews();
        
        for (Event e : trip.events) {
			RelativeLayout eventItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.trip_event_item, llContent, false);
			((TextView)eventItem.findViewById(R.id.tvTitle)).setText(e.title);
			
			if(!e.address.equals("null"))
				((TextView)eventItem.findViewById(R.id.tvAddress)).setText(e.address);
			else
				((TextView)eventItem.findViewById(R.id.tvAddress)).setText("");
			
			ImageView icon = (ImageView) eventItem.findViewById(R.id.imageIcon);
			switch (e.type) {
			case START:
				icon.setImageResource(R.drawable.marker_start);
				break;
			case STOP:
				icon.setImageResource(R.drawable.marker_finish);
				break;
			case HARSH_BRAKING:
				icon.setImageResource(R.drawable.marker_brake);
				break;
			case HARSH_ACCELERATION:
				icon.setImageResource(R.drawable.marker_accel);
				break;
			case PHONE_USAGE:
				icon.setImageResource(R.drawable.marker_phone);
				break;
			case APP_USAGE:
				icon.setImageResource(R.drawable.marker_app);
				break;
			default:
				break;
			}
			
			llEventsList.addView(eventItem);
		}

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
		outState.putLong(EXTRA_TRIP_ID, tripId);
		super.onSaveInstanceState(outState);
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
    
    class GetTripTask extends BaseRequestAsyncTask{
		
		public GetTripTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			scrollView.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			scrollView.setVisibility(View.VISIBLE);
		}

		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("driver_id", ""+driver.id));
	        requestParams.add(new BasicNameValuePair("trip_id", ""+tripId));
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			trip = new Trip(tripId, 0, Utils.fixTimezoneZ(responseJSON.getString("start_time")), Utils.fixTimezoneZ(responseJSON.getString("end_time")), responseJSON.getDouble("mileage"));
			trip.averageSpeed = responseJSON.getDouble("avg_speed");
			trip.maxSpeed = responseJSON.getDouble("max_speed");
				
			JSONArray routeJSON = responseJSON.getJSONArray("route");
			for (int i = 0; i < routeJSON.length(); i++) {
				JSONArray pointJSON = routeJSON.getJSONArray(i);
				trip.route.add(new LatLng(pointJSON.getDouble(0), pointJSON.getDouble(1)));					
			}
				
			JSONArray pointsJSON = responseJSON.getJSONArray("points");
			for (int i = 0; i < pointsJSON.length(); i++) {
				JSONObject pointJSON = pointsJSON.getJSONObject(i);
				JSONObject locationJSON = pointJSON.getJSONObject("location");
				JSONArray eventsJSON = pointJSON.getJSONArray("events");
					
				ArrayList<EventType> events = new ArrayList<EventType>();
				for (int j = 0; j < eventsJSON.length(); j++) {
					EventType type = getEventType(eventsJSON.getString(j));
					if(type!=null)
						events.add(type);
				}
				
				trip.points.add(new Point(new LatLng(locationJSON.getDouble("latitude"), locationJSON.getDouble("longitude")), events));
			}
			
			JSONArray eventsJSON = responseJSON.getJSONArray("events");
			for (int i = 0; i < eventsJSON.length(); i++) {
				JSONObject eventJSON = eventsJSON.getJSONObject(i);
				
				EventType type = getEventType(eventJSON.getString("type"));
				if(type!=null)
					trip.events.add(new Event(type, eventJSON.getString("title"), eventJSON.getString("address")));					
			}
				
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
			case "phone_usage":
				return EventType.PHONE_USAGE;
			case "app_usage":
				return EventType.APP_USAGE;
			case "speeding":
				break;
			default:
				break;
			}
			return null;
		}
	}
}