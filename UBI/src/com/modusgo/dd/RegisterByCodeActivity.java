package com.modusgo.dd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.InitActivity;
import com.modusgo.ubi.MainActivity;
import com.farmers.ubi.R;
import com.modusgo.ubi.utils.Utils;

public class RegisterByCodeActivity extends FragmentActivity {
    
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	View layoutProgress;
	
	SharedPreferences prefs;
	
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "1054950055033";
    String regid;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_by_code);
	    getActionBar().hide();
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(RegisterByCodeActivity.this);
	    
	    
	    if(prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
			Toast.makeText(getApplicationContext(), "You should login first", Toast.LENGTH_SHORT).show();
	    	startActivity(new Intent(this, InitActivity.class));
			finish();
	    }
	    else{
		    checkRegCode(getIntent());
	    }
	    
	    ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.rotate);
	    a.setDuration(1000);
	    pb.startAnimation(a);
	    
	    context = getApplicationContext();
	}
	
	private void checkRegCode(Intent i){
		if(i!=null && i.getData()!=null && 
				prefs.getString(Constants.PREF_REG_CODE, "").equals("") && !prefs.getString(Constants.PREF_AUTH_KEY, "").equals("")){
			Uri data = i.getData();
			String modus_user_id = data.getQueryParameter(getQueryParameterNames(data).toArray()[0].toString());
			registerStart(modus_user_id);
		}
		else{
			Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	public void registerStart(String regCode){
		
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(RegisterByCodeActivity.this);
            regid = getRegistrationId(context);

            if (regid.equals("")) {
            	// TestFlight.log("No GCM id saved in prefs, starting registration");
                registerInBackground(regCode);
            }
            else{
            	// TestFlight.log("GCM id exist in prefs");
            	registerSuccess();
            }
        } else {
            registerError("No valid Google Play Services APK found.");
        }
	}
	
	public void registerSuccess(){
		Intent i = new Intent(getApplicationContext(), InitActivity.class);
		startActivity(i);
		Toast.makeText(getApplicationContext(), "Code registration successful!", Toast.LENGTH_SHORT).show();
        finish();
	}
	
	public void registerError(String error){
		Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void registerInBackground(final String regCode) {
        new AsyncTask<Void, Void, String>() {
        	
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend(regCode);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (Exception ex) {
                    msg = "Error: " + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if(msg.contains("Error")){
                	registerError(msg);
                }
                else{
                	registerSuccess();
                }
            	//mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
	
	private void sendRegistrationIdToBackend(String regCode)  throws JSONException, URISyntaxException, ClientProtocolException, IOException, UserRegistrationServerError{
    	JSONObject json = new JSONObject();
    	URL url = new URL(Constants.REGISTRATION_BY_CODE_URL);
    	
    	if(!regCode.equals("")){
    		json.put("regcode",regCode);
    		json.put("push_id",regid);
    	    json.put("platform","android");
    	    json.put("mobile_id",Utils.getUUID(getApplicationContext()));
    	}
    	
	    
    	// POST
	    DefaultHttpClient httpClient = new DefaultHttpClient();
	    HttpPost httpPost;
		
			httpPost = new HttpPost(url.toURI());
			// Prepare JSON to send by setting the entity
		    httpPost.setEntity(new StringEntity(json.toString(), "UTF-8"));

		    // Set up the header types needed to properly transfer JSON
		    httpPost.setHeader("Content-Type", "application/json");
		    httpPost.setHeader("Accept-Encoding", "application/json");
		    httpPost.setHeader("Accept-Language", "en-US");
            //Basic access authentication
            String encoding = Base64.encodeToString((Constants.API_AUTH_LOGIN+":"+Constants.API_AUTH_PASS).getBytes(), Base64.NO_WRAP);
            httpPost.setHeader("Authorization", "Basic " + encoding);

		    // Execute POST
		    HttpResponse httpResponse = httpClient.execute(httpPost);
		    BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
		    String jsonResponse = reader.readLine();
		    JSONTokener tokener = new JSONTokener(jsonResponse);
		    JSONObject finalResult = new JSONObject(tokener);
		    
		    if(httpResponse.getStatusLine().getStatusCode()>=HttpStatus.SC_OK && httpResponse.getStatusLine().getStatusCode()<=299){
		    	if(!finalResult.isNull("status") && !finalResult.isNull("name") && !finalResult.isNull("phone")){    
			    	
			    	Editor e = prefs.edit();
					e.putString(Constants.PREF_REG_CODE, regCode);
					e.commit();
		    	}
			    else{
			    	throw new UserRegistrationServerError(finalResult.getString("error"));
			    }
			    
		    }
		    else{
		    	throw new UserRegistrationServerError(finalResult.getString("error"));
		    }
		
    }
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.equals("")) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
	
	private Set<String> getQueryParameterNames(Uri uri) {
        if (uri.isOpaque()) {
            throw new UnsupportedOperationException("This isn't a hierarchical URI.");
        }

        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }
	
	private class UserRegistrationServerError extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// Constructor that accepts a message
		public UserRegistrationServerError(String message) {
			super(message);
		}
	}
}
