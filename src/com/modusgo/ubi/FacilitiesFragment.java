package com.modusgo.ubi;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.ubi.customviews.GoogleMapFragment;
import com.modusgo.ubi.customviews.GoogleMapFragment.OnMapReadyListener;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FacilitiesFragment extends Fragment implements OnMapReadyListener {

	private Vehicle vehicle;
	private SharedPreferences prefs;
	private View rootView = null;
	private GoogleMapFragment mMapFragment = null;
	private GoogleMap mMap = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MainActivity main = (MainActivity)getActivity();
		
		View rootView = inflater.inflate(R.layout.activity_find_mechanic, container, false);

		main.setActionBarTitle("Facilities");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(main);

		vehicle = ((DriverActivity)getActivity()).vehicle;
		
		/**************DEBUGGING ONLY ***************/
		String loc = "CarMax\r\n2000 Frontage Road \r\nNorthbrook, IL \r\n60062";
		/**************DEBUGGING ONLY ***************/

		if(mMap != null)
		{
			mMap.clear();
			setUpMap();
		}
		else
		{
			setUpMapIfNeeded();
		}


//		TextView tv = (TextView) findViewById(R.id.find_mechanic_details);
//		tv.setText(loc);
		
//		viewFlipper.setDisplayedChild(findMechanicViewIndex);
		
		return rootView;
	}

	private void setUpMapIfNeeded()
	{
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if(mMap == null)
		{
			mMapFragment = new GoogleMapFragment();
			getChildFragmentManager().beginTransaction().replace(R.id.mapContainer, mMapFragment)
					.commitAllowingStateLoss();
		}
	}

	private void setUpMap()
	{
		if(vehicle.latitude != 0 && vehicle.longitude != 0)
		{
			mMap.addMarker(new MarkerOptions().position(new LatLng(vehicle.latitude, vehicle.longitude)).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
			float density = 1;
			if(isAdded())
				density = getResources().getDisplayMetrics().density;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(vehicle.latitude - 0.016f / density,
					vehicle.longitude), 14.0f));
		}

		mMap.getUiSettings().setZoomControlsEnabled(false);
	}
	@Override
	public void onMapReady()
	{
		mMap = mMapFragment.getMap();
		if(mMap != null)
			setUpMap();
	}
}
