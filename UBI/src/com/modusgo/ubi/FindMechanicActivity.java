package com.modusgo.ubi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

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
import com.google.maps.android.ui.IconGenerator;

public class FindMechanicActivity extends MainActivity {
	
	public static final String EXTRA_TRIP_ID = "tripId";
	
	//Driver driver;
	//DriversHelper dHelper;
	int driverIndex = 0;
	
	MapView mapView;
    GoogleMap map;
    
    LinearLayout llContent;
    ListView listView;
    LinearLayout llProgress;
    
    ArrayList<MechanicInfo> mechanics;
    MechanicsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_find_mechanic);
		super.onCreate(savedInstanceState);
		
		setActionBarTitle("Find Mechanic");
		
		if(savedInstanceState!=null){
			driverIndex = savedInstanceState.getInt("id");
		}
		else if(getIntent()!=null){
			driverIndex = getIntent().getIntExtra("id",0);
		}

		//dHelper = DriversHelper.getInstance();
		//driver = dHelper.getDriverByIndex(driverIndex);

		llProgress = (LinearLayout) findViewById(R.id.llProgress);
		llContent = (LinearLayout) findViewById(R.id.llContent);
		listView = (ListView) findViewById(R.id.listView);
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);

        MapsInitializer.initialize(this);
        
        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.8430094,-95.0098992), 1);
        map.animateCamera(cameraUpdate);
        
        map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				updateMap();
			}
		});
        
        mechanics = new ArrayList<MechanicInfo>();
        adapter = new MechanicsAdapter(this, R.layout.find_mechanic_item, mechanics);
        listView.setAdapter(adapter);

		new ReadJSONTask().execute();
	}
	
	private void updateMap(){
		if(mechanics.size()>0){	
			map.clear();
			Builder builder = LatLngBounds.builder();
			for (MechanicInfo mi : mechanics) {
				map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(mi.icon)).position(mi.coordinate));
				builder.include(mi.coordinate);
			}
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 150);
	        map.animateCamera(cameraUpdate);
		}
        
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("id", driverIndex);
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
    
    class MechanicInfo{
    	
    	float raiting;
        int reviewsCount;
        String title;
        String phoneNumber;
        int index;
        String address;
        LatLng coordinate;
        Bitmap icon;
        
		public MechanicInfo(float raiting, int reviewsCount, String title,
				String phoneNumber, int index, String address, LatLng coordinate, Bitmap icon) {
			super();
			this.raiting = raiting;
			this.reviewsCount = reviewsCount;
			this.title = title;
			this.phoneNumber = phoneNumber;
			this.index = index;
			this.address = address;
			this.coordinate = coordinate;
			this.icon = icon;
		}
    }
    
    class MechanicsAdapter extends ArrayAdapter<MechanicInfo>{

		public MechanicsAdapter(Context context, int resource, List<MechanicInfo> objects) {
			super(context, resource, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder holder;
			
			if(view==null){
				view = getLayoutInflater().inflate(R.layout.find_mechanic_item, parent, false);
				holder = new ViewHolder();
				holder.icon = (ImageView) view.findViewById(R.id.imageIcon);
				holder.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
				holder.tvReviewCount = (TextView) view.findViewById(R.id.tvReviewCount);
				holder.tvPhone = (TextView) view.findViewById(R.id.tvPhone);
				holder.tvAddress = (TextView) view.findViewById(R.id.tvAddress);
				holder.rating = (RatingBar) view.findViewById(R.id.ratingBar);
				view.setTag(holder);
			}
			else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			final MechanicInfo mi = getItem(position);
			
			holder.icon.setImageBitmap(mi.icon);
			holder.tvTitle.setText(""+mi.title);
			holder.tvReviewCount.setText(""+mi.reviewsCount);
			holder.tvAddress.setText(""+mi.address);
			holder.tvPhone.setText(""+mi.phoneNumber);
			holder.rating.setRating(mi.raiting);
			
			((View)holder.tvAddress.getParent()).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mi.coordinate, 15);
			        map.animateCamera(cameraUpdate);
				}
			});
			
			((View)holder.tvPhone.getParent()).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent callIntent = new Intent(Intent.ACTION_VIEW);          
		            callIntent.setData(Uri.parse("tel:"+mi.phoneNumber));          
		            startActivity(callIntent);  
				}
			});
			
			return view;
		}
    	
    }
    
    private static class ViewHolder{
    	ImageView icon;
    	TextView tvTitle;
    	TextView tvReviewCount;
    	TextView tvPhone;
    	TextView tvAddress;
    	RatingBar rating;
    }
    
    class ReadJSONTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			llContent.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String json = null;
	        try {

	            InputStream is = getAssets().open("mechanics.json");
	            int size = is.available();
	            byte[] buffer = new byte[size];
	            is.read(buffer);
	            is.close();
	            json = new String(buffer, "UTF-8");

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        try{
	        	JSONArray mechanicsJSON = new JSONArray(json);
	        	mechanics.clear();
	        	
	            IconGenerator iconFactory = new IconGenerator(FindMechanicActivity.this);
	            iconFactory.setBackground(getResources().getDrawable(R.drawable.marker_for_number));
	            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
	            
	        	for (int i = 0; i < mechanicsJSON.length(); i++) {

	        		JSONObject mJSON = mechanicsJSON.getJSONObject(i);
		        	mechanics.add(new MechanicInfo(
		        			(float) mJSON.getDouble("raiting"), 
		        			mJSON.getInt("reviewsCount"), 
		        			mJSON.getString("title"), 
		        			mJSON.getString("phoneNumber"), 
		        			mJSON.getInt("index"), 
		        			mJSON.getString("adress"), 
		        			new LatLng(mJSON.getJSONArray("coordinate").getDouble(0), mJSON.getJSONArray("coordinate").getDouble(1)), 
		        			iconFactory.makeIcon(""+mJSON.getInt("index"), 10, Color.WHITE, typeface)));
				}
	        	
	        	
	        }
	        catch(JSONException e){
	        	e.printStackTrace();
	        }
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			llContent.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}
}