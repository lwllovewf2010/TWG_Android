package com.modusgo.twg;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.twg.R;
import com.modusgo.twg.customviews.GoogleMapFragment;
import com.modusgo.twg.customviews.GoogleMapFragment.OnMapReadyListener;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
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
	private MainActivity main = null;

	/**************DEBUGGING ONLY ***************/
	String locHTML = "<h2>CarMax</h2>" +  
			"2000 Frontage Road<br>" +
			"Northbrook, IL 60062<br>" +
			"Open today	10:00 am – 9:00 pm <br>" +
			"<a href=\"carmax.com\">carmax.com</a><br>" +
			"<a href=\"tel:8472420045\">(847) 242-0045</a>";
	double latitude = 42.11823;
	double longitude = -87.77935;
	/**************DEBUGGING ONLY ***************/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		main = (MainActivity)getActivity();
		
		View rootView = inflater.inflate(R.layout.activity_find_mechanic, container, false);

		main.setActionBarTitle("Facilities");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(main);

		vehicle = ((DriverActivity)getActivity()).vehicle;

		setUpMapIfNeeded();


		TextView tv = (TextView) rootView.findViewById(R.id.mechanic_details);
		Spanned result = Html.fromHtml(locHTML);
		tv.setText(result);
		
		main.showBusyDialog(R.string.RetrievingMapData);
		return rootView;
	}

	private void setUpMapIfNeeded()
	{
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if(mMap == null)
		{
			mMapFragment = new GoogleMapFragment();
			getChildFragmentManager().beginTransaction().replace(R.id.mapview, mMapFragment)
					.commitAllowingStateLoss();
		}
	}


	private void setUpMap()
	{
		if(latitude != 0 && longitude != 0)
		{
			mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
			float density = 1;
			if(isAdded())
				density = getResources().getDisplayMetrics().density;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude - 0.016f / density,
					longitude), 14.0f));
		}

		mMap.getUiSettings().setZoomControlsEnabled(false);
	}
	
	@Override
	public void onMapReady()
	{
		main.hideBusyDialog();
		
		mMap = mMapFragment.getMap();
		if(mMap != null)
			setUpMap();
	}
}
