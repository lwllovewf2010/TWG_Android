package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;

import org.apache.http.message.BasicNameValuePair;
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

public class SettingsEditFragment extends Fragment {

	public static final String EXTRA_FIRST_NAME = "first_name";
	public static final String EXTRA_LAST_NAME = "last_name";
	public static final String EXTRA_PHONE = "phone_number";
	public static final String EXTRA_EMAIL = "email";
	public static final String EXTRA_TIMEZONE = "time_zone";
	public static final String EXTRA_PASSWORD = "password";
	public static final String PREF_JUSTSAVED = "justsaved";
	
	private static final String TAG_CHANGED = "changed";

	EditText editFirstName;
	EditText editLastName;
	EditText editPhone;
	EditText editEmail;
	Spinner spinnerTimezone;
	EditText editPassword;
	Button btnUpdate;
	Button btnCancel;
	
	private int spinnerDefault = 0;
	private boolean spinnerChanged = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings_edit, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SETTINGS");
		
		editFirstName = (EditText)rootView.findViewById(R.id.editFirstName);
		editLastName = (EditText)rootView.findViewById(R.id.editLastName);
		editPhone = (EditText)rootView.findViewById(R.id.editPhone);
		editEmail = (EditText)rootView.findViewById(R.id.editEmail);
		spinnerTimezone = (Spinner)rootView.findViewById(R.id.spinnerTimezone);
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		editPassword = (EditText)rootView.findViewById(R.id.editPassword);
		final EditText editConfirmPassword = (EditText)rootView.findViewById(R.id.editConfirmPassword);
		final TextView tvPasswordError = (TextView)rootView.findViewById(R.id.tvPasswordError);
		btnUpdate = (Button)rootView.findViewById(R.id.btnUpdate);
		btnCancel = (Button)rootView.findViewById(R.id.btnCancel);
		
		ArrayList<String> timezones = getTimezoneList();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.simple_spinner_item, timezones) {
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
		};
		
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinnerTimezone.setAdapter(adapter);
		spinnerTimezone.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position!=spinnerDefault){
					((TextView)view).setTextColor(Color.parseColor("#00aeef"));
					spinnerChanged = true;
				}
				else{
					((TextView)view).setTextColor(Color.parseColor("#697078"));
					spinnerChanged = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		
		if(savedInstanceState!=null){
			editFirstName.setText(savedInstanceState.getString(EXTRA_FIRST_NAME));
			editLastName.setText(savedInstanceState.getString(EXTRA_LAST_NAME));
			editPhone.setText(savedInstanceState.getString(EXTRA_PHONE));
			editEmail.setText(savedInstanceState.getString(EXTRA_EMAIL));
			String tz = savedInstanceState.getString(EXTRA_TIMEZONE);
			for (int i = 0; i < timezones.size(); i++) {
				if(timezones.get(i).contains(tz)){
					spinnerTimezone.setSelection(i);
					spinnerDefault = i;
					break;
				}
			}
		}
		else if(getArguments()!=null){
			//driverIndex = getArguments().getInt("id");
			editFirstName.setText(getArguments().getString(EXTRA_FIRST_NAME));
			editLastName.setText(getArguments().getString(EXTRA_LAST_NAME));
			editPhone.setText(getArguments().getString(EXTRA_PHONE));
			editEmail.setText(getArguments().getString(EXTRA_EMAIL));
			String tz = getArguments().getString(EXTRA_TIMEZONE);
			for (int i = 0; i < timezones.size(); i++) {
				if(timezones.get(i).contains(tz)){
					spinnerTimezone.setSelection(i);
					spinnerDefault = i;
					break;
				}
			}
		}
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		imagePhoto.setImageResource(R.drawable.person_placeholder);
		
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
		
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
					new SetSettingsTask(getActivity()).execute("settings.json");	
				}
				else{
					tvPasswordError.setVisibility(View.VISIBLE);
					editConfirmPassword.setTextColor(Color.parseColor("#ef4136"));
				}
			}
		});
		
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				getActivity().getSupportFragmentManager().popBackStack();			
			}
		});
		
		prefs.edit().putBoolean(PREF_JUSTSAVED, true).commit();
		
		return rootView;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		outState.putString(EXTRA_FIRST_NAME, editFirstName.getText().toString());
		outState.putString(EXTRA_LAST_NAME, editLastName.getText().toString());
		outState.putString(EXTRA_PHONE, editPhone.getText().toString());
		outState.putString(EXTRA_EMAIL, editEmail.getText().toString());
		outState.putString(EXTRA_TIMEZONE, ((TextView)spinnerTimezone.getSelectedView()).getText().toString());
		
		super.onSaveInstanceState(outState);
	}
	
	public static ArrayList<String> getTimezoneList(){
		ArrayList<String> timezoneIDs= new ArrayList<String>();
		ArrayList<String> timezoneNames = new ArrayList<String>();
		
		timezoneIDs.add("Pacific/Majuro");
		timezoneIDs.add("Pacific/Midway");
		timezoneIDs.add("Pacific/Honolulu");
		timezoneIDs.add("America/Anchorage");
		timezoneIDs.add("America/Los_Angeles");
		timezoneIDs.add("America/Tijuana");
		timezoneIDs.add("America/Phoenix");
		timezoneIDs.add("America/Chihuahua");
		timezoneIDs.add("America/Denver");
		timezoneIDs.add("America/Costa_Rica");
		timezoneIDs.add("America/Chicago");
		timezoneIDs.add("America/Mexico_City");
		timezoneIDs.add("America/Regina");
		timezoneIDs.add("America/Bogota");
		timezoneIDs.add("America/New_York");
		timezoneIDs.add("America/Caracas");
		timezoneIDs.add("America/Barbados");
		timezoneIDs.add("America/Halifax");
		timezoneIDs.add("America/Manaus");
		timezoneIDs.add("America/Santiago");
		timezoneIDs.add("America/St_Johns");
		timezoneIDs.add("America/Sao_Paulo");
		timezoneIDs.add("America/Argentina/Buenos_Aires");
		timezoneIDs.add("America/Godthab");
		timezoneIDs.add("America/Montevideo");
		timezoneIDs.add("Atlantic/South_Georgia");
		timezoneIDs.add("Atlantic/Azores");
		timezoneIDs.add("Atlantic/Cape_Verde");
		timezoneIDs.add("Africa/Casablanca");
		timezoneIDs.add("Europe/London");
		timezoneIDs.add("Europe/Amsterdam");
		timezoneIDs.add("Europe/Belgrade");
		timezoneIDs.add("Europe/Brussels");
		timezoneIDs.add("Europe/Sarajevo");
		timezoneIDs.add("Africa/Windhoek");
		timezoneIDs.add("Africa/Brazzaville");
		timezoneIDs.add("Asia/Amman");
		timezoneIDs.add("Europe/Athens");
		timezoneIDs.add("Asia/Beirut");
		timezoneIDs.add("Africa/Cairo");
		timezoneIDs.add("Europe/Helsinki");
		timezoneIDs.add("Asia/Jerusalem");
		timezoneIDs.add("Europe/Minsk");
		timezoneIDs.add("Africa/Harare");
		timezoneIDs.add("Asia/Baghdad");
		timezoneIDs.add("Europe/Moscow");
		timezoneIDs.add("Asia/Kuwait");
		timezoneIDs.add("Africa/Nairobi");
		timezoneIDs.add("Asia/Tehran");
		timezoneIDs.add("Asia/Baku");
		timezoneIDs.add("Asia/Tbilisi");
		timezoneIDs.add("Asia/Yerevan");
		timezoneIDs.add("Asia/Dubai");
		timezoneIDs.add("Asia/Kabul");
		timezoneIDs.add("Asia/Karachi");
		timezoneIDs.add("Asia/Oral");
		timezoneIDs.add("Asia/Yekaterinburg");
		timezoneIDs.add("Asia/Calcutta");
		timezoneIDs.add("Asia/Colombo");
		timezoneIDs.add("Asia/Katmandu");
		timezoneIDs.add("Asia/Almaty");
		timezoneIDs.add("Asia/Rangoon");
		timezoneIDs.add("Asia/Krasnoyarsk");
		timezoneIDs.add("Asia/Bangkok");
		timezoneIDs.add("Asia/Shanghai");
		timezoneIDs.add("Asia/Hong_Kong");
		timezoneIDs.add("Asia/Irkutsk");
		timezoneIDs.add("Asia/Kuala_Lumpur");
		timezoneIDs.add("Australia/Perth");
		timezoneIDs.add("Asia/Taipei");
		timezoneIDs.add("Asia/Seoul");
		timezoneIDs.add("Asia/Tokyo");
		timezoneIDs.add("Asia/Yakutsk");
		timezoneIDs.add("Australia/Adelaide");
		timezoneIDs.add("Australia/Darwin");
		timezoneIDs.add("Australia/Brisbane");
		timezoneIDs.add("Australia/Hobart");
		timezoneIDs.add("Australia/Sydney");
		timezoneIDs.add("Asia/Vladivostok");
		timezoneIDs.add("Pacific/Guam");
		timezoneIDs.add("Asia/Magadan");
		timezoneIDs.add("Pacific/Auckland");
		timezoneIDs.add("Pacific/Fiji");
		timezoneIDs.add("Pacific/Tongatapu");
		
		timezoneNames.add("Marshall Islands");
	    timezoneNames.add("Midway Island");
	    timezoneNames.add("Hawaii");
	    timezoneNames.add("Alaska");
	    timezoneNames.add("Pacific Time");
	    timezoneNames.add("Tijuana");
	    timezoneNames.add("Arizona");
	    timezoneNames.add("Chihuahua");
	    timezoneNames.add("Mountain Time");
	    timezoneNames.add("Central America");
	    timezoneNames.add("Central Time");
	    timezoneNames.add("Mexico City");
	    timezoneNames.add("Saskatchewan");
	    timezoneNames.add("Bogota");
	    timezoneNames.add("Eastern Time");
	    timezoneNames.add("Venezuela");
	    timezoneNames.add("Atlantic Time (Barbados)");
	    timezoneNames.add("Atlantic Time (Canada)");
	    timezoneNames.add("Manaus");
	    timezoneNames.add("Santiago");
	    timezoneNames.add("Newfoundland");
	    timezoneNames.add("Brasilia");
	    timezoneNames.add("Buenos Aires");
	    timezoneNames.add("Greenland");
	    timezoneNames.add("Montevideo");
	    timezoneNames.add("Mid-Atlantic");
	    timezoneNames.add("Azores");
	    timezoneNames.add("Cape Verde Islands");
	    timezoneNames.add("Casablanca");
	    timezoneNames.add("London, Dublin");
	    timezoneNames.add("Amsterdam, Berlin");
	    timezoneNames.add("Belgrade");
	    timezoneNames.add("Brussels");
	    timezoneNames.add("Sarajevo");
	    timezoneNames.add("Windhoek");
	    timezoneNames.add("W. Africa Time");
	    timezoneNames.add("Amman, Jordan");
	    timezoneNames.add("Athens, Istanbul");
	    timezoneNames.add("Beirut, Lebanon");
	    timezoneNames.add("Cairo");
	    timezoneNames.add("Helsinki");
	    timezoneNames.add("Jerusalem");
	    timezoneNames.add("Minsk");
	    timezoneNames.add("Harare");
	    timezoneNames.add("Baghdad");
	    timezoneNames.add("Moscow");
	    timezoneNames.add("Kuwait");
	    timezoneNames.add("Nairobi");
	    timezoneNames.add("Tehran");
	    timezoneNames.add("Baku");
	    timezoneNames.add("Tbilisi");
	    timezoneNames.add("Yerevan");
	    timezoneNames.add("Dubai");
	    timezoneNames.add("Kabul");
	    timezoneNames.add("Islamabad, Karachi");
	    timezoneNames.add("Ural'sk");
	    timezoneNames.add("Yekaterinburg");
	    timezoneNames.add("Kolkata");
	    timezoneNames.add("Sri Lanka");
	    timezoneNames.add("Kathmandu");
	    timezoneNames.add("Astana");
	    timezoneNames.add("Yangon");
	    timezoneNames.add("Krasnoyarsk");
	    timezoneNames.add("Bangkok");
	    timezoneNames.add("Beijing");
	    timezoneNames.add("Hong Kong");
	    timezoneNames.add("Irkutsk");
	    timezoneNames.add("Kuala Lumpur");
	    timezoneNames.add("Perth");
	    timezoneNames.add("Taipei");
	    timezoneNames.add("Seoul");
	    timezoneNames.add("Tokyo, Osaka");
	    timezoneNames.add("Yakutsk");
	    timezoneNames.add("Adelaide");
	    timezoneNames.add("Darwin");
	    timezoneNames.add("Brisbane");
	    timezoneNames.add("Hobart");
	    timezoneNames.add("Sydney, Canberra");
	    timezoneNames.add("Vladivostok");
	    timezoneNames.add("Guam");
	    timezoneNames.add("Magadan");
	    timezoneNames.add("Auckland");
	    timezoneNames.add("Fiji");
	    timezoneNames.add("Tonga");
		
		
		ArrayList<String> timezonesPlus = new ArrayList<String>();
		ArrayList<String> timezones = new ArrayList<String>();
		
		for (int i = 0; i< timezoneIDs.size(); i++) {
			TimeZone tz = TimeZone.getTimeZone(timezoneIDs.get(i));
			int hours = Math.abs(tz.getRawOffset()) / 3600000;
		    int minutes = Math.abs(tz.getRawOffset() / 60000) % 60;
		    String sign = tz.getRawOffset() >= 0 ? "+" : "-";
		      
			if(sign.equals("+"))
				timezonesPlus.add(String.format("GMT %s%02d:%02d %s", sign, hours, minutes, timezoneNames.get(i)));
			else
				timezones.add(String.format("GMT %s%02d:%02d %s", sign, hours, minutes, timezoneNames.get(i)));
		}
		
		Collections.sort(timezonesPlus.subList(0, timezonesPlus.size()));
		Collections.reverse(timezones);
		Collections.sort(timezones.subList(0, timezones.size()));
		
		timezones.addAll(timezonesPlus);
		
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
	
	class SetSettingsTask extends BasePostRequestAsyncTask{
		
		public SetSettingsTask(Context context) {
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
	        	if(editFirstName.getTag()!=null && editFirstName.getTag().equals(TAG_CHANGED))
	        		requestParams.add(new BasicNameValuePair(EXTRA_FIRST_NAME, editFirstName.getText().toString()));
	        	if(editLastName.getTag()!=null && editLastName.getTag().equals(TAG_CHANGED))
	        		requestParams.add(new BasicNameValuePair(EXTRA_LAST_NAME, editLastName.getText().toString()));
	        	if(editPhone.getTag()!=null && editPhone.getTag().equals(TAG_CHANGED))
	        		requestParams.add(new BasicNameValuePair(EXTRA_PHONE, editPhone.getText().toString()));
	        	if(editEmail.getTag()!=null && editEmail.getTag().equals(TAG_CHANGED))
	        		requestParams.add(new BasicNameValuePair(EXTRA_EMAIL, editEmail.getText().toString()));
	        	
		        if(spinnerChanged){
		        	String tz = ((TextView)spinnerTimezone.getSelectedView()).getText().toString();
		        	tz = tz.substring(0,10).replace(" ", "");
		        	requestParams.add(new BasicNameValuePair(EXTRA_TIMEZONE, tz));
		        }
		        if(editPassword.getTag()!=null && editPassword.getTag().equals(TAG_CHANGED))
		        	requestParams.add(new BasicNameValuePair(EXTRA_PASSWORD, editPassword.getText().toString()));
		        System.out.println(requestParams);
	        }
	        catch(NullPointerException e){
	        	e.printStackTrace();
	        }
	        
			return super.doInBackground(params);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			Editor e = prefs.edit();
			e.putString(EXTRA_FIRST_NAME, editFirstName.getText().toString());
			e.putString(EXTRA_LAST_NAME, editLastName.getText().toString());
			e.putString(EXTRA_PHONE, editPhone.getText().toString());
			e.putString(EXTRA_EMAIL, editEmail.getText().toString());
			e.putString(EXTRA_TIMEZONE, ((TextView)spinnerTimezone.getSelectedView()).getText().toString());
			e.commit();
			

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
