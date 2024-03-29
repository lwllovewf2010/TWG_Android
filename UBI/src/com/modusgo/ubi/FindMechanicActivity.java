package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.maps.android.ui.IconGenerator;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;

public class FindMechanicActivity extends MainActivity implements ConnectionCallbacks,
OnConnectionFailedListener, LocationListener{

	public static final String EXTRA_TRIP_ID = "tripId";

	MapView mapView;
	GoogleMap map;

	ListView listView;
	LinearLayout llProgress;

	ArrayList<MechanicInfo> mechanics;
	MechanicsAdapter adapter;

	private GoogleApiClient mGoogleApiClient; 

	LatLng currentLocation;

	// These settings are the same as the settings for the map. They will in fact give you updates
	// at the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(3000)    // every 3 seconds
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_find_mechanic);
		super.onCreate(savedInstanceState);

		setActionBarTitle("Find Mechanic");

		//dHelper = DriversHelper.getInstance();
		//driver = dHelper.getDriverByIndex(driverIndex);

		llProgress = (LinearLayout) findViewById(R.id.llProgress);
		listView = (ListView) findViewById(R.id.listView);

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

			map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
				@Override
				public void onMapLoaded() {
					updateMap();
				}
			});
		}

		mechanics = new ArrayList<MechanicInfo>();
		adapter = new MechanicsAdapter(this, R.layout.find_mechanic_item, mechanics);
		listView.setAdapter(adapter);

		setUpLocationClientIfNeeded();
		mGoogleApiClient.connect();
	}

	private void updateMap(){
		if(map!=null && mechanics.size()>0){	
			map.clear();
			Builder builder = LatLngBounds.builder();
			for (MechanicInfo mi : mechanics) {
				map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(mi.icon)).position(mi.coordinate));
				builder.include(mi.coordinate);
			}

			try{
				int mapPadding = (int) Math.min(mapView.getHeight()*0.2f, mapView.getWidth()*0.2f);
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), mapPadding);
				map.animateCamera(cameraUpdate);
			}
			catch(IllegalStateException e){
				CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLocation, 10, 0, 0));
				map.animateCamera(cameraUpdate);
			}
		}

	}

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
		setNavigationDrawerItemSelected(MenuItems.FINDAMECHANIC);
		Utils.gaTrackScreen(this, "Find A Mechanic Screen");
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

	private void setUpLocationClientIfNeeded() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addApi(LocationServices.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();// OnConnectionFailedListener
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
		new GetMechanicsTask(this).execute("find_mechanic.json");

		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		//Do nothing
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}

	class MechanicInfo{

		float rating;
		int reviewsCount;
		String title;
		String phoneNumber;
		int index;
		String address;
		LatLng coordinate;
		Bitmap icon;

		public MechanicInfo(float rating, int reviewsCount, String title,
				String phoneNumber, int index, String address, LatLng coordinate, Bitmap icon) {
			super();
			this.rating = rating;
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
			if(!mi.phoneNumber.equals(""))
				holder.tvPhone.setText(""+mi.phoneNumber);
			else{
				((View)holder.tvPhone.getParent()).setVisibility(View.GONE);
			}
			holder.rating.setRating(mi.rating);

			((View)holder.tvAddress.getParent()).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mi.coordinate, 15);
					if(map!=null)
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

	class GetMechanicsTask extends BaseRequestAsyncTask{

		public GetMechanicsTask(Context context) {
			super(context);
			requestParams.add(new BasicNameValuePair("latitude", ""+currentLocation.latitude));
			requestParams.add(new BasicNameValuePair("longitude", ""+currentLocation.longitude));
		}

		@Override
		protected void onPreExecute() {
			llProgress.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			JSONArray mechanicsJSON = responseJSON.getJSONArray("results");
			mechanics.clear();

			IconGenerator iconFactory = new IconGenerator(FindMechanicActivity.this);
			iconFactory.setBackground(getResources().getDrawable(R.drawable.marker_for_number));
			Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");

			for (int i = 0; i < mechanicsJSON.length(); i++) {

				JSONObject mJSON = mechanicsJSON.getJSONObject(i);
				mechanics.add(new MechanicInfo(
						(float) mJSON.optDouble("rating"), 
						mJSON.optInt("reviewsCount"), 
						mJSON.optString("name"), 
						mJSON.optString("phoneNumber"), 
						i+1, 
						mJSON.optString("vicinity"), 
						new LatLng(mJSON.optDouble("lat"), mJSON.optDouble("lng")), 
						iconFactory.makeIcon(""+(i+1), 10, Color.WHITE, typeface)));
			}

			updateMap();

			super.onSuccess(responseJSON);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			llProgress.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}
}