package com.modusgo.twg;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.modusgo.twg.R;
import com.modusgo.twg.customviews.GoogleMapFragment;
import com.modusgo.twg.customviews.GoogleMapFragment.OnMapReadyListener;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.db.VehicleContract.VehicleEntry;
import com.modusgo.twg.requesttasks.BaseRequestAsyncTask;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DriverDetailsFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, OnMapReadyListener
{
	Vehicle vehicle;
	SharedPreferences prefs;

	TextView tvName;
	TextView tvVehicle;
	TextView tvLocation;
	TextView tvDate;
	ImageView imagePhoto;
	TextView tvFuelLevel = null;
	TextView tvOilLife = null;
	TextView tvEngineTemperature = null;
	TextView tvBatteryOutput = null;

	View btnFuelLevel;
	View rlVehicleDate;
	View spaceFuel;

	Button callServiceBtn = null;

	private GoogleMapFragment mMapFragment;
	private GoogleMap mMap;

	private LocationClient mLocationClient;

	private MainActivity main = null;
	
	// These settings are the same as the settings for the map. They will in
	// fact give you updates
	// at the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create().setInterval(5000) // 5
																								// seconds
			.setFastestInterval(3000) // every 3 seconds
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.fragment_driver_details, container, false);

		main = ((MainActivity) getActivity());
		main.setActionBarTitle(getResources().getString(R.string.app_name));

		prefs = PreferenceManager.getDefaultSharedPreferences(main);

		vehicle = ((DriverActivity) getActivity()).vehicle;

		tvName = (TextView) rootView.findViewById(R.id.tvName);
		tvVehicle = (TextView) rootView.findViewById(R.id.tvVehicle);
		tvLocation = (TextView) rootView.findViewById(R.id.tvLocation);
		tvDate = (TextView) rootView.findViewById(R.id.tvDate);
		imagePhoto = (ImageView) rootView.findViewById(R.id.imagePhoto);

		tvFuelLevel = (TextView) rootView.findViewById(R.id.tvFuel);
		tvOilLife = (TextView) rootView.findViewById(R.id.tvOilLife);
		tvEngineTemperature = (TextView) rootView.findViewById(R.id.tvEngineTemperature);
		tvBatteryOutput = (TextView) rootView.findViewById(R.id.tvBattery);
		// btnDistanceToCar = (View) tvDistanceToCar.getParent();
		rlVehicleDate = rootView.findViewById(R.id.rlDate);
		spaceFuel = rootView.findViewById(R.id.spaceFuel);
		callServiceBtn = (Button) rootView.findViewById(R.id.callServiceBtn);

		callServiceBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String uri = "tel: 18005551212";
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse(uri));
				startActivity(intent);
			}
		});

//		main.showBusyDialog(R.string.RetrievingMapData);
		
		updateFragment();


		rootView.findViewById(R.id.rlLocation).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getActivity(), MapActivity.class);
				intent.putExtra(VehicleEntry._ID, vehicle.id);
				startActivity(intent);
			}
		});

		imagePhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(getActivity(), SettingsActivity.class));
			}
		});

		return rootView;
	}

	@Override
	public void onMapReady()
	{
//		main.hideBusyDialog();
		
		mMap = mMapFragment.getMap();
		if(mMap != null)
			setUpMap();
	}

	private void updateFragment()
	{
		try
		{
			if(mMap != null)
			{
				mMap.clear();
				setUpMap();
			}
			updateDriverInfo();

			setUpLocationClientIfNeeded();
			mLocationClient.connect();
		} catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	
	
	private void updateDriverInfo()
	{
		ColorFilter filter = null;
		Drawable d = null;

		tvName.setText(vehicle.name);
		tvVehicle.setText(vehicle.getCarFullName());
		if(vehicle.address == null || vehicle.address.equals(""))
			tvLocation.setText("Unknown location");
		else
			tvLocation.setText(vehicle.address);

		SimpleDateFormat sdfFrom = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault());
		SimpleDateFormat sdfTo = new SimpleDateFormat("MM/dd/yyyy KK:mm aa z", Locale.getDefault());

		TimeZone tzFrom = TimeZone.getTimeZone(Constants.DEFAULT_TIMEZONE);
		sdfFrom.setTimeZone(tzFrom);
		TimeZone tzTo = TimeZone.getTimeZone(prefs
				.getString(Constants.PREF_TIMEZONE_OFFSET, Constants.DEFAULT_TIMEZONE));
		sdfTo.setTimeZone(tzTo);

		try
		{
			tvDate.setText(sdfTo.format(sdfFrom.parse(vehicle.lastTripDate)));
		} catch(ParseException e)
		{
			tvDate.setText(vehicle.lastTripDate);
			e.printStackTrace();
		}

		if(vehicle.photo == null || vehicle.photo.equals(""))
			imagePhoto.setImageResource(R.drawable.person_placeholder);
		else
		{
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.person_placeholder)
					.showImageForEmptyUri(R.drawable.person_placeholder).showImageOnFail(R.drawable.person_placeholder)
					.cacheInMemory(true).cacheOnDisk(true).build();

			ImageLoader.getInstance().displayImage(vehicle.photo, imagePhoto, options);
		}

		/*-------------------------Fuel Level------------------------*/
		View fuelBlock = (View) tvFuelLevel.getParent();

		if(vehicle.carFuelLevel >= 0 && !TextUtils.isEmpty(vehicle.carFuelUnit))
		{
			String fuelLeftString = vehicle.carFuelLevel + vehicle.carFuelUnit;
			int fuelUnitLength = vehicle.carFuelUnit.length();
			SpannableStringBuilder cs = new SpannableStringBuilder(fuelLeftString);
			cs.setSpan(new SuperscriptSpan(), fuelLeftString.length() - fuelUnitLength, fuelLeftString.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			cs.setSpan(new RelativeSizeSpan(0.5f), fuelLeftString.length() - fuelUnitLength, fuelLeftString.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tvFuelLevel.setText(cs);
			tvFuelLevel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fuel_green, 0, 0, 0);
			fuelBlock.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Toast.makeText(getActivity(),
							"The percentage shown is the last known fuel level reported from your vehicle.",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else
		{
			if(!TextUtils.isEmpty(vehicle.carFuelStatus))
			{
				tvFuelLevel.setText("");
				tvFuelLevel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fuel_green, 0,
						R.drawable.ic_fuel_arrow_down, 0);
				fuelBlock.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Toast.makeText(getActivity(), vehicle.carFuelStatus, Toast.LENGTH_SHORT).show();
					}
				});
			} else
			{
				spaceFuel.setVisibility(View.GONE);
				fuelBlock.setVisibility(View.GONE);
			}
		}

		/*------------------------Oil Life--------------------------------*/

		/************************ DEBUGGING ONLY ****************************/
		final String vehicleOilLevel = "28%";
		final String vehicleOilUnit = "";
		final String vehiclecarOilStatus = "";
		/************************ DEBUGGING ONLY ****************************/

		if(!TextUtils.isEmpty(vehicleOilLevel)/*
											 * vehicleOilLevel = 0 &&
											 * !TextUtils.
											 * isEmpty(vehicleOilUnit)
											 */)
		{
			// String OilLeftString = vehicleOilLevel + vehicleOilUnit;
			// int OilUnitLength = vehicleOilUnit.length();
			// SpannableStringBuilder cs = new SpannableStringBuilder(
			// OilLeftString);
			// cs.setSpan(new SuperscriptSpan(), OilLeftString.length()
			// - OilUnitLength, OilLeftString.length(),
			// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			// cs.setSpan(new RelativeSizeSpan(0.5f), OilLeftString.length()
			// - OilUnitLength, OilLeftString.length(),
			// Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			// tvOilLife.setText(cs);
			// tvOilLife.setText(vehicleOilLevel);
			d = getResources().getDrawable(R.drawable.ic_oil);
			filter = new LightingColorFilter(Color.BLACK, Color.GREEN);
			d.setColorFilter(filter);

			tvOilLife.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
			tvOilLife.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					((DriverActivity) getActivity()).tabHost.setCurrentTab(2);
				}
			});
		} else
		{
			if(!TextUtils.isEmpty(vehiclecarOilStatus))
			{
				tvOilLife.setText("");
				tvOilLife.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_oil, 0, R.drawable.ic_fuel_arrow_down,
						0);
			} else
			{
				tvOilLife.setText("N/A");
				tvOilLife.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_oil, 0, 0, 0);
			}
		}

		/*-------------------------Engine Temperature-----------------------*/
		d = getResources().getDrawable(R.drawable.ic_coolant);
		filter = new LightingColorFilter(Color.BLACK, Color.RED);
		d.setColorFilter(filter);
		tvEngineTemperature.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
		tvEngineTemperature.setClickable(true);
		tvEngineTemperature.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				((DriverActivity) getActivity()).tabHost.setCurrentTab(3);
			}
		});

		/*-------------------------Battery Output---------------------------*/
		d = getResources().getDrawable(R.drawable.ic_battery);
		filter = new LightingColorFilter(Color.BLACK, Color.GREEN);
		d.setColorFilter(filter);
		tvBatteryOutput.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
		tvBatteryOutput.setClickable(true);
		tvBatteryOutput.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				((DriverActivity) getActivity()).tabHost.setCurrentTab(3);
			}
		});


		// if(vehicle.carDTCCount <= 0)
		// {
		// tvDiagnostics.setText("");
		// tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_diagnostics_green_medium, 0, 0, 0);
		// } else
		// {
		// tvDiagnostics.setText("" + vehicle.carDTCCount);
		// tvDiagnostics.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_diagnostics_red_medium, 0, 0, 0);
		// }
		//
		// if(vehicle.alerts <= 0)
		// {
		// tvAlerts.setText("");
		// tvAlerts.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_alerts_green_medium, 0, 0, 0);
		// } else
		// {
		// tvAlerts.setText("" + vehicle.alerts);
		// tvAlerts.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_alerts_red_medium, 0, 0, 0);
		// }

		//----------------Vehicle Last Trip--------------------
		if(vehicle.lastTripId > 0)
		{
//			rlVehicleDate.findViewById(R.id.imageArrow).setVisibility(View.VISIBLE);
			rlVehicleDate.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent intent = new Intent(getActivity(), TripActivity.class);
					intent.putExtra(VehicleEntry._ID, vehicle.id);
					intent.putExtra(TripActivity.EXTRA_TRIP_ID, vehicle.lastTripId);
					startActivity(intent);
				}
			});
		} else
		{
//			rlVehicleDate.findViewById(R.id.imageArrow).setVisibility(View.GONE);
		}

		if(TextUtils.isEmpty(vehicle.lastTripDate))
		{
			rlVehicleDate.setVisibility(View.GONE);
		}

		setUpMapIfNeeded();
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
		
	private void setUpLocationClientIfNeeded()
	{
		if(mLocationClient == null)
		{
			mLocationClient = new LocationClient(getActivity().getApplicationContext(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	float distanceToCar[] = new float[3];
	DecimalFormat dsitanceFormat = new DecimalFormat("0.0");

	@Override
	public void onLocationChanged(Location location)
	{

		if(mLocationClient != null)
		{
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint)
	{
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected()
	{
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		// Do nothing
	}

	@Override
	public void onResume()
	{
		new GetVehiclesTask(getActivity()).execute("vehicles/" + vehicle.id + ".json");
		Utils.gaTrackScreen(getActivity(), "Driver Details Screen");
		super.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(mLocationClient != null)
		{
			mLocationClient.disconnect();
		}
	}

	class GetVehiclesTask extends BaseRequestAsyncTask
	{

		public GetVehiclesTask(Context context)
		{
			super(context);
		}

		@Override
		protected void onError(String message)
		{
			// Do nothing
		}

		@Override
		protected JSONObject doInBackground(String... params)
		{
			return super.doInBackground(params);
		}

		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException
		{
			System.out.println(responseJSON);

			JSONObject vehicleJSON = responseJSON.getJSONObject("vehicle");
			if(isAdded())
			{
				vehicle = Vehicle.fromJSON(getActivity().getApplicationContext(), vehicleJSON);
				DbHelper dbHelper = DbHelper.getInstance(getActivity());
				dbHelper.saveVehicle(vehicle);
				dbHelper.close();

				updateFragment();
			}
			// new
			// GetTripsTask(context).execute("vehicles/"+vehicle.id+"/trips.json");

			super.onSuccess(responseJSON);
		}
	}
}