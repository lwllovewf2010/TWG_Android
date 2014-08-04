package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsEditFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings_edit, null);
		
		((MainActivity)getActivity()).setActionBarTitle("SETTINGS");
		
		EditText editFirstName = (EditText)rootView.findViewById(R.id.editFirstName);
		EditText editLastName = (EditText)rootView.findViewById(R.id.editLastName);
		EditText editPhone = (EditText)rootView.findViewById(R.id.editPhone);
		EditText editEmail = (EditText)rootView.findViewById(R.id.editEmail);
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		final EditText editPassword = (EditText)rootView.findViewById(R.id.editPassword);
		final EditText editConfirmPassword = (EditText)rootView.findViewById(R.id.editConfirmPassword);
		final TextView tvPasswordError = (TextView)rootView.findViewById(R.id.tvPasswordError);
		Button btnUpdate = (Button)rootView.findViewById(R.id.btnUpdate);
		Button btnCancel = (Button)rootView.findViewById(R.id.btnCancel);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Driver d = DbHelper.getDrivers().get(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0));
		
		editFirstName.setText(d.getFirstName());
		editLastName.setText(d.getLastName());
		editPhone.setText(d.phone);
		editEmail.setText(d.email);
		imagePhoto.setImageResource(d.imageId);
		
		editFirstName.addTextChangedListener(new CustomTextWatcher(editFirstName));
		editLastName.addTextChangedListener(new CustomTextWatcher(editLastName));
		editPhone.addTextChangedListener(new CustomTextWatcher(editPhone));
		editEmail.addTextChangedListener(new CustomTextWatcher(editEmail));
		editPassword.addTextChangedListener(new CustomTextWatcher(editPassword));
		editConfirmPassword.addTextChangedListener(new CustomTextWatcher(editConfirmPassword));
		
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
				timezonesPlus.add(String.format("GMT %s %02d:%02d %s", sign, hours, minutes, timezoneNames.get(i)));
			else
				timezones.add(String.format("GMT %s %02d:%02d %s", sign, hours, minutes, timezoneNames.get(i)));
		}
		
		Collections.sort(timezonesPlus.subList(0, timezonesPlus.size()));
		Collections.reverse(timezones);
		Collections.sort(timezones.subList(0, timezones.size()));
		
		timezones.addAll(timezonesPlus);
		
		Spinner spinnerTimezone = (Spinner)rootView.findViewById(R.id.spinnerTimezone);
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
		spinnerTimezone.setSelection(22);
		
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
					getActivity().getSupportFragmentManager().popBackStack();	
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
		
		return rootView;
	}
	
	private class CustomTextWatcher implements TextWatcher {
	    private EditText mEditText;

	    public CustomTextWatcher(EditText e) { 
	        mEditText = e;
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	    }

	    public void afterTextChanged(Editable s) {
	    	mEditText.setTextColor(Color.parseColor("#00aeef"));
	    }
	}
}
