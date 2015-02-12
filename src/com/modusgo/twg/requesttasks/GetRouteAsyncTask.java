/**
 * 
 */
package com.modusgo.twg.requesttasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author yaturner
 *
 */
public class GetRouteAsyncTask extends AsyncTask<Void, Void, ArrayList<LatLng>>
{
	private final static String TAG = "GetRouteAsyncTask";

	protected Context context;
	protected SharedPreferences prefs;
	int status = 0;
	String message = "";
	protected String baseUrl;
	protected boolean checkSuccess = true;
	protected ArrayList<LatLng> lines = null;

	protected List<NameValuePair> requestParams = new ArrayList<NameValuePair>();

	public GetRouteAsyncTask(Context context, final LatLng origin, final LatLng destination)
	{
		this.context = context.getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		baseUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + ","
				+ origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude
				+ "&sensor=false";
		Log.d(TAG, "URL = '" + baseUrl + "'");
		lines = new ArrayList<LatLng>();
	}

	@Override
	protected ArrayList<LatLng> doInBackground(Void... params)
	{
		HttpResponse response = null;
		HttpGet request = null;
		InputStream source = null;
		String returnValue = null;
		JSONObject result = null;

		AndroidHttpClient client = AndroidHttpClient.newInstance("routeUserAgant");

		request = new HttpGet(baseUrl);
		try
		{
			response = client.execute(request);
			source = response.getEntity().getContent();
			returnValue = buildString(source);
		} catch(IOException e)
		{
			e.printStackTrace();
			return lines;
		}
		
		client.close();
		client = null;

		try
		{
			result = new JSONObject(returnValue);
		} catch(JSONException e)
		{
			e.printStackTrace();
			return lines;
		}

		if(result != null && result.length() > 0)
		{
			lines = decodeRoutes(result);
		}

		return lines;

	}

	@Override
	protected void onPostExecute(ArrayList<LatLng> result)
	{
		super.onPostExecute(result);
	}

	private String buildString(InputStream is)
	{
		StringBuilder total = null;
		try
		{
			total = new StringBuilder(is.available());
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = r.readLine()) != null)
			{
				total.append(line);
			}
		} catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return total.toString();
	}
	
	private ArrayList<LatLng> decodeRoutes(JSONObject result)
	{
		JSONArray routes = null;
		JSONArray steps = null;
		
		try
		{
			routes = result.getJSONArray("routes");
			long distanceForSegment = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
					.getJSONObject("distance").getInt("value");
			steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
			lines = new ArrayList<LatLng>();
			for(int i = 0; i < steps.length(); i++)
			{
				String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
				for(LatLng p : decodePolyline(polyline))
				{
					lines.add(p);
				}
			}
		} catch(JSONException e)
		{
			e.printStackTrace();
		}
		
		return lines;
	}

	private ArrayList<LatLng> decodePolyline(String encoded)
	{

		ArrayList<LatLng> poly = new ArrayList<LatLng>();

		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while(index < len)
		{
			int b, shift = 0, result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while(b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while(b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(p);
		}

		return poly;
	}

}