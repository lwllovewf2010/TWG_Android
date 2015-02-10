package com.modusgo.twg;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
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
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.modusgo.twg.R;
import com.modusgo.twg.customviews.GoogleMapFragment;
import com.modusgo.twg.customviews.GoogleMapFragment.OnMapReadyListener;
import com.modusgo.twg.requesttasks.BaseRequestAsyncTask;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;

public class FindMechanicActivity extends MainActivity implements OnMapReadyListener
{
	private SharedPreferences prefs;
	private GoogleMapFragment mMapFragment = null;
	private GoogleMap mMap = null;

	/**************DEBUGGING ONLY ***************/
	String locHTML = "<h2>CarMax</h2>" +  
			"2000 Frontage Road<br>" +
			"Northbrook, IL 60062<br>" +
			"Open today	10:00 am – 9:00 pm <br>" +
			"<a href=\"http://carmax.com\">carmax.com</a><br>" +
			"<a href=\"tel:8472420045\">(847) 242-0045</a>";
	double latitude = 42.11823;
	double longitude = -87.77935;
	/**************DEBUGGING ONLY ***************/

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.activity_find_mechanic);
		super.onCreate(savedInstanceState);

		setActionBarTitle("Find Mechanic");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		setUpMapIfNeeded();


		TextView tv = (TextView) findViewById(R.id.mechanic_details);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		Spanned result = Html.fromHtml(locHTML);
		tv.setText(result);
		
		showBusyDialog(R.string.RetrievingMapData);
	}

	private void setUpMapIfNeeded()
	{
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if(mMap == null)
		{
//			FragmentManager mgr = getFragmentManager();
//			map = ((MapFragment) getFragmentSupportManager().findFragmentById(R.id.map))
//			        .getMap();
//			mMapFragment = new GoogleMapFragment();
//			mMapFragment.getMap();
//			getChildFragmentManager().beginTransaction().replace(R.id.mapview, mMapFragment)
//					.commitAllowingStateLoss();
		}
	}


	private void setUpMap()
	{
		if(latitude != 0 && longitude != 0)
		{
			mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.marker_car)));
			float density = 1;
//			if(isAdded())
//				density = getResources().getDisplayMetrics().density;
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude - 0.016f / density,
					longitude), 14.0f));
		}

		mMap.getUiSettings().setZoomControlsEnabled(false);
	}
	
	@Override
	public void onMapReady()
	{
		hideBusyDialog();
		
		mMap = mMapFragment.getMap();
		if(mMap != null)
			setUpMap();
	}

}