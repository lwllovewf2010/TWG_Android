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
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.requesttasks.BaseRequestAsyncTask;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;

public class FindMechanicActivity extends MainActivity //implements OnMapReadyListener, OnMapLoadedCallback 
{
	private SharedPreferences prefs;
	private GoogleMapFragment mMapFragment = null;
	private MapView mapView;
    private GoogleMap map;
    

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
		
		setActionBarTitle(getResources().getString(R.string.FindMechanic));
		
		// Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 14);
        map.animateCamera(cameraUpdate);
 //       map.setOnMapLoadedCallback(new OnMapLoadedCallback() {
//			@Override
//			public void onMapLoaded() {
//				CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 14);
//		        map.animateCamera(cameraUpdate);
		        addMarkerToMap();
//			}
//		});
        
        MapsInitializer.initialize(this);    
        
        TextView tv = (TextView) findViewById(R.id.mechanic_details);
        tv.setText(Html.fromHtml(locHTML));

	}
	
	private void addMarkerToMap(){
		LatLng location = new LatLng(latitude, longitude);
		map.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_car)).title(getResources().getString(R.string.CarMax)));
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
        map.animateCamera(cameraUpdate);
	}
	
	@Override
    public void onResume() {
        mapView.onResume();
        Utils.gaTrackScreen(this, "Current Location Screen");
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

}