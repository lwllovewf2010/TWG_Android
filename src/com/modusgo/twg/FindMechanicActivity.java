package com.modusgo.twg;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.requesttasks.GetRouteAsyncTask;
import com.modusgo.twg.utils.Utils;

public class FindMechanicActivity extends MainActivity 
{
	private final static String TAG = "FindMechanicActivity";
	
	private MapView mapView;
	private GoogleMap map;
	private ArrayList<LatLng> lines = null;
	private PolylineOptions options = null;
	private long vehicleId;

	/************** DEBUGGING ONLY ***************/
	String locHTML = "<h2>CarMax</h2>" + "2000 Frontage Road<br>" + "Northbrook, IL 60062<br>"
			+ "Open today	10:00 am – 9:00 pm <br>";
	String linkHTML =  "<a href=\"http://carmax.com\">carmax.com</a><br>"
			+ "<a href=\"tel:8472420045\">(847) 242-0045</a>";
	double latitude = 42.11823;
	double longitude = -87.77935;

	/************** DEBUGGING ONLY ***************/

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.activity_find_mechanic);
		super.onCreate(savedInstanceState);

		setActionBarTitle(getResources().getString(R.string.FindMechanic));

		if(savedInstanceState!=null){
			vehicleId = savedInstanceState.getLong(VehicleEntry._ID);
		}
		else if(getIntent()!=null){
			vehicleId = getIntent().getLongExtra(VehicleEntry._ID,0);
		}

		DbHelper dHelper = DbHelper.getInstance(this);
		vehicle = dHelper.getVehicleShort(vehicleId);
		dHelper.close();
		

		
		final LatLng destination = new LatLng(latitude, longitude);
		final LatLng origin = new LatLng(vehicle.latitude, vehicle.longitude);

		GetRouteAsyncTask task = new GetRouteAsyncTask(this, origin, destination);
		task.execute();
		try
		{
			lines = task.get();
		} catch(InterruptedException | ExecutionException e)
		{
			lines = new ArrayList<LatLng>();
			e.printStackTrace();
		}

		// Gets the MapView from the XML layout and creates it
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.onCreate(savedInstanceState);

		// Gets to GoogleMap from the MapView and does initialization stuff
		map = mapView.getMap();
		map.getUiSettings().setMyLocationButtonEnabled(false);
		map.setOnMapLoadedCallback(new OnMapLoadedCallback()
		{
			
			@Override
			public void onMapLoaded()
			{
				addMarkerToMap();
				if(lines.size() > 0)
				{
					options = new PolylineOptions();
					options.addAll(lines);
					options.width(15f);
					options.color(getResources().getColor(R.color.red));
				}
			}
		});

		MapsInitializer.initialize(this);

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14);
		map.animateCamera(cameraUpdate);
		
		TextView tv = (TextView) findViewById(R.id.mechanic_address_details);
		tv.setText(Html.fromHtml(locHTML));
		tv.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				LatLngBounds bounds = LatLngBounds.builder().include(origin).include(destination).build();
				Polyline polylineToAdd = map.addPolyline(options);
				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
				map.animateCamera(cameraUpdate);
			}
		});

		
		tv = (TextView) findViewById(R.id.mechanic_hyperlink_details);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		Spanned result = Html.fromHtml(linkHTML);
		tv.setText(result);
		

		Button navBtn = (Button)findViewById(R.id.navigation_btn);
		navBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
					    Uri.parse("http://maps.google.com/maps?saddr=" +
					    		origin.latitude + "," + origin.longitude +
					    		"&daddr= " +
					    		destination.latitude + "," + destination.longitude ));
					startActivity(intent);
			}
		});

	}

	private void addMarkerToMap()
	{
		LatLng location = new LatLng(latitude, longitude);
		map.addMarker(new MarkerOptions().position(location)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car))
				.title(getResources().getString(R.string.CarMax)));
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
		map.animateCamera(cameraUpdate);
	}


	@Override
	public void onResume()
	{
		mapView.onResume();
		Utils.gaTrackScreen(this, "Current Location Screen");
		super.onResume();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		mapView.onLowMemory();
	}

}