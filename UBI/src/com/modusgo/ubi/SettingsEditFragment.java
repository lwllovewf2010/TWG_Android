package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsEditFragment extends Fragment {
	
	private static final String TAG_CHANGED = "changed";

	EditText editFirstName;
	EditText editLastName;
	EditText editPhone;
	EditText editEmail;
	Spinner spinnerTimezone;
	Spinner spinnerCar;
	EditText editPassword;
	Button btnUpdate;
	Button btnCancel;
	
	private int spinnerTimezoneDefault = 0;
	private int spinnerCarDefault = 0;
	private boolean spinnerTimezoneChanged = false;
	private boolean spinnerCarChanged = false;
	
	ArrayList<Vehicle> vehicles;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings_edit, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SETTINGS");
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		editFirstName = (EditText)rootView.findViewById(R.id.editFirstName);
		editLastName = (EditText)rootView.findViewById(R.id.editLastName);
		editPhone = (EditText)rootView.findViewById(R.id.editPhone);
		editEmail = (EditText)rootView.findViewById(R.id.editEmail);
		spinnerTimezone = (Spinner)rootView.findViewById(R.id.spinnerTimezone);
		spinnerCar = (Spinner)rootView.findViewById(R.id.spinnerCar);
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		editPassword = (EditText)rootView.findViewById(R.id.editPassword);
		final EditText editConfirmPassword = (EditText)rootView.findViewById(R.id.editConfirmPassword);
		final TextView tvPasswordError = (TextView)rootView.findViewById(R.id.tvPasswordError);
		btnUpdate = (Button)rootView.findViewById(R.id.btnUpdate);
		btnCancel = (Button)rootView.findViewById(R.id.btnCancel);
		
		editFirstName.setText(prefs.getString(Constants.PREF_FIRST_NAME, ""));
		editLastName.setText(prefs.getString(Constants.PREF_LAST_NAME, ""));
		editPhone.setText(prefs.getString(Constants.PREF_PHONE, ""));
		editEmail.setText(prefs.getString(Constants.PREF_EMAIL, ""));
		
		String photoURL = prefs.getString(Constants.PREF_PHOTO, "");
		
		if(TextUtils.isEmpty(photoURL))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(photoURL, imagePhoto, options);
	    }
		
		ArrayList<String> timezones = getTimezoneList();
		
		TypefacedArrayAdapter<String> adapterTimezones = new TypefacedArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item, timezones);
        adapterTimezones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinnerTimezone.setAdapter(adapterTimezones);
		spinnerTimezoneDefault = adapterTimezones.getPosition(prefs.getString(Constants.PREF_TIMEZONE, ""));
		spinnerTimezone.setSelection(spinnerTimezoneDefault);
		spinnerTimezone.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position!=spinnerTimezoneDefault){
					((TextView)view).setTextColor(Color.parseColor("#00aeef"));
					spinnerTimezoneChanged = true;
				}
				else{
					((TextView)view).setTextColor(Color.parseColor("#697078"));
					spinnerTimezoneChanged = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		vehicles = dbHelper.getVehiclesShort();
		dbHelper.close();
		
		ArrayList<String> cars = new ArrayList<String>();
		long vehicleId = prefs.getLong(Constants.PREF_VEHICLE_ID, -1);
		String currentVehicle = "";
		
		for (Vehicle vehicle : vehicles) {
			cars.add(vehicle.getCarFullName());
			if(vehicle.id==vehicleId)
				currentVehicle = vehicle.getCarFullName();
		}
		
		TypefacedArrayAdapter<String> adapterCars = new TypefacedArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item, cars);
		adapterCars.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCar.setAdapter(adapterCars);
		spinnerCarDefault = adapterCars.getPosition(currentVehicle);
		spinnerCar.setSelection(spinnerCarDefault);
		
		if(!prefs.getString(Constants.PREF_ROLE, "").equals(Constants.ROLE_CUSTOMER) || vehicleId>0){
			spinnerCar.setEnabled(false);
		}
		else{
			spinnerCar.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					if(position!=spinnerCarDefault){
						((TextView)view).setTextColor(Color.parseColor("#00aeef"));
						spinnerCarChanged = true;
					}
					else{
						((TextView)view).setTextColor(Color.parseColor("#697078"));
						spinnerCarChanged = false;
					}
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
		}
		
		editFirstName.addTextChangedListener(new CustomTextWatcher(editFirstName));
		editLastName.addTextChangedListener(new CustomTextWatcher(editLastName));
		editPhone.addTextChangedListener(new CustomTextWatcher(editPhone));
		editEmail.addTextChangedListener(new CustomTextWatcher(editEmail));
		editPassword.addTextChangedListener(new CustomTextWatcher(editPassword));
		editConfirmPassword.addTextChangedListener(new CustomTextWatcher(editConfirmPassword));
		
		editPassword.setTransformationMethod(new AsteriskPasswordTransformationMethod());
		editConfirmPassword.setTransformationMethod(new AsteriskPasswordTransformationMethod());
		
		ImageView btnPhoto = (ImageView)rootView.findViewById(R.id.btnPhoto);
		btnPhoto.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
					v.setAlpha(0.5f);
				if(event.getAction()==MotionEvent.ACTION_UP){
					v.setAlpha(1f);
					v.performClick();
				}
				return true;
			}
		});		
		

		btnUpdate.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, "#f15b2a")));
		try{
			btnUpdate.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, "#edf1f9")));
		}
	    catch(Exception e){
	    	e.printStackTrace();
	    }
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
					new SetDriverTask(getActivity()).execute("driver.json");	
				}
				else{
					tvPasswordError.setVisibility(View.VISIBLE);
					editConfirmPassword.setTextColor(Color.parseColor("#ef4136"));
				}
			}
		});
		
		try{
			btnCancel.setTextColor(Utils.getButtonTextColorStateList(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, "#f15b2a")));
		}
	    catch(Exception e){
	    	e.printStackTrace();
	    }
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				getActivity().getSupportFragmentManager().popBackStack();			
			}
		});
		
		return rootView;
	}
	
	private ArrayList<String> getTimezoneList(){
		ArrayList<String> timezones = new ArrayList<String>();
		JSONObject tzJSON = Utils.getJSONObjectFromAssets(getActivity(), "timezones.json");
		try{
			JSONArray tzJSONArray = tzJSON.getJSONArray("timezones");
			int tzCount = tzJSONArray.length();
			for (int i = 0; i < tzCount; i++) {
				timezones.add(tzJSONArray.getString(i));
			}
		}
		catch(JSONException e){
			e.printStackTrace();
		}
		return timezones;
	}
	
	private class CustomTextWatcher implements TextWatcher {
	    private EditText mEditText;
	    private String defaultValue;

	    public CustomTextWatcher(EditText e) { 
	        mEditText = e;
	        defaultValue = mEditText.getText().toString();
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	    }

	    public void afterTextChanged(Editable s) {
	    	if(!mEditText.getText().toString().equals(defaultValue)){
		    	mEditText.setTextColor(Color.parseColor("#00aeef"));
		    	mEditText.setTag(TAG_CHANGED);
	    	}
	    	else{
		    	mEditText.setTextColor(Color.parseColor("#697078"));
		    	mEditText.setTag(null);	    		
	    	}
	    }
	}
	
	class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
	    @Override
	    public CharSequence getTransformation(CharSequence source, View view) {
	        return new PasswordCharSequence(source);
	    }

	    private class PasswordCharSequence implements CharSequence {
	        private CharSequence mSource;
	        public PasswordCharSequence(CharSequence source) {
	            mSource = source; // Store char sequence
	        }
	        public char charAt(int index) {
	            return '*'; // This is the important part
	        }
	        public int length() {
	            return mSource.length(); // Return default
	        }
	        public CharSequence subSequence(int start, int end) {
	            return mSource.subSequence(start, end); // Return default
	        }
	    }
	};
	
	class TypefacedArrayAdapter<T> extends ArrayAdapter<T>{

		public TypefacedArrayAdapter(Context context, int resource,
				List<T> objects) {
			super(context, resource, objects);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView v = (TextView) super.getView(position, convertView, parent);

			Typeface externalFont = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
			v.setTypeface(externalFont);
			v.setTextColor(Color.parseColor("#697078"));
			v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

			return v;
		}

		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			TextView v = (TextView)super.getDropDownView(position, convertView, parent);

			Typeface externalFont = Typeface.createFromAsset(getActivity()
					.getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
			v.setTypeface(externalFont);
			v.setTextColor(Color.parseColor("#697078"));
			v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

			return v;
		}
	}
	
	class SetDriverTask extends BasePostRequestAsyncTask{
		
		public SetDriverTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			btnUpdate.setVisibility(View.INVISIBLE);
			btnCancel.setEnabled(false);
			btnCancel.setText("Updating...");
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			btnUpdate.setVisibility(View.VISIBLE);
			btnCancel.setEnabled(true);
			btnCancel.setText("Cancel");
			super.onPostExecute(result);
		}

		@Override
		protected JSONObject doInBackground(String... params) {

	        try{
        		requestParams.add(new BasicNameValuePair(Constants.PREF_DRIVER_ID, ""+prefs.getLong(Constants.PREF_DRIVER_ID, 0)));
	        	
	        	requestParams.add(new BasicNameValuePair(Constants.PREF_FIRST_NAME, editFirstName.getText().toString()));
	        	requestParams.add(new BasicNameValuePair(Constants.PREF_LAST_NAME, editLastName.getText().toString()));
	        	requestParams.add(new BasicNameValuePair(Constants.PREF_EMAIL, editEmail.getText().toString()));
	        	
	        	if(editPhone.getTag()!=null && editPhone.getTag().equals(TAG_CHANGED))
	        		requestParams.add(new BasicNameValuePair(Constants.PREF_PHONE, editPhone.getText().toString()));
	        	
		        if(spinnerTimezoneChanged){
		        	String tz = ((TextView)spinnerTimezone.getSelectedView()).getText().toString();
		        	tz = tz.substring(0,10).replace(" ", "");
		        	requestParams.add(new BasicNameValuePair(Constants.PREF_TIMEZONE, tz));
		        }
		        if(spinnerCarChanged){
		        	for (Vehicle v : vehicles) {
		        		if(v.getCarFullName().equals(spinnerCar.getSelectedItem())){
		        			requestParams.add(new BasicNameValuePair(Constants.PREF_VEHICLE_ID, ""+v.id));
		        			break;
		        		}
					}
		        }
		        if(editPassword.getTag()!=null && editPassword.getTag().equals(TAG_CHANGED))
		        	requestParams.add(new BasicNameValuePair("password", editPassword.getText().toString()));
		        System.out.println(requestParams);
	        }
	        catch(NullPointerException e){
	        	e.printStackTrace();
	        }
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			System.out.println(responseJSON);
			
			Editor e = prefs.edit();
			Long vehicleId = responseJSON.optLong(Constants.PREF_VEHICLE_ID);
			String photo = responseJSON.optString(Constants.PREF_PHONE);
			e.putLong(Constants.PREF_DRIVER_ID, responseJSON.optLong(Constants.PREF_DRIVER_ID));
			e.putLong(Constants.PREF_VEHICLE_ID, vehicleId);
			e.putString(Constants.PREF_FIRST_NAME, responseJSON.optString(Constants.PREF_FIRST_NAME));
			e.putString(Constants.PREF_LAST_NAME, responseJSON.optString(Constants.PREF_LAST_NAME));
			e.putString(Constants.PREF_EMAIL, responseJSON.optString(Constants.PREF_EMAIL));
			e.putString(Constants.PREF_ROLE, responseJSON.optString(Constants.PREF_ROLE));
			e.putString(Constants.PREF_PHONE, photo);
			e.putString(Constants.PREF_TIMEZONE, responseJSON.optString(Constants.PREF_TIMEZONE));
			e.putString(Constants.PREF_PHOTO, responseJSON.optString(Constants.PREF_PHOTO));
			e.commit();
			
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			Vehicle d = dbHelper.getVehicle(vehicleId);
			d.photo = photo;
			dbHelper.saveVehicle(d);
			dbHelper.close();

			btnUpdate.setVisibility(View.INVISIBLE);
			btnCancel.setEnabled(false);
			btnCancel.setText("Updated");
			
			try{
				getActivity().getSupportFragmentManager().popBackStack();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
	}
}
