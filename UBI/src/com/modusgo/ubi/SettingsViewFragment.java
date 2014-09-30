package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.modusgo.demo.R;

public class SettingsViewFragment extends Fragment{	
	
	ProgressBar progress;
	LinearLayout llMainInfo;
	ScrollView svAdditionalInfo;
	
	TextView tvFirstName;
	TextView tvLastName;
	TextView tvPhone;
	TextView tvEmail;
	TextView tvTimezone;
	
	private String email;
	
	SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings_view, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SETTINGS");
		
		progress = (ProgressBar) rootView.findViewById(R.id.progressBar);
		llMainInfo = (LinearLayout) rootView.findViewById(R.id.llMainInfo);
		svAdditionalInfo = (ScrollView) rootView.findViewById(R.id.svAdditionalInfo);
		
		tvFirstName = (TextView)rootView.findViewById(R.id.tvFirstName);
		tvLastName = (TextView)rootView.findViewById(R.id.tvLastName);
		tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
		tvEmail = (TextView)rootView.findViewById(R.id.tvEmail);
		tvTimezone = (TextView)rootView.findViewById(R.id.tvTimezone);
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		
		tvTimezone.setText("");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		imagePhoto.setImageResource(R.drawable.person_placeholder);
		
		final SettingsEditFragment sef = new SettingsEditFragment();
		
		
		Button btnEdit = (Button)rootView.findViewById(R.id.btnEdit);
		btnEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bundle b = new Bundle();
				b.putString(SettingsEditFragment.EXTRA_FIRST_NAME, tvFirstName.getText().toString());
				b.putString(SettingsEditFragment.EXTRA_LAST_NAME, tvLastName.getText().toString());
				b.putString(SettingsEditFragment.EXTRA_PHONE, tvPhone.getText().toString());
				b.putString(SettingsEditFragment.EXTRA_EMAIL, email);
				b.putString(SettingsEditFragment.EXTRA_TIMEZONE, tvTimezone.getText().toString());
				sef.setArguments(b);
				
				getActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.content_frame, sef)
				.addToBackStack(null)
				.commit();
			}
		});
		
		
		System.out.println("saved null = "+(savedInstanceState == null));
		if(prefs.getBoolean(SettingsEditFragment.PREF_JUSTSAVED, false)==true){
			updateFields(
					prefs.getString(SettingsEditFragment.EXTRA_FIRST_NAME,""), 
					prefs.getString(SettingsEditFragment.EXTRA_LAST_NAME,""), 
					prefs.getString(SettingsEditFragment.EXTRA_PHONE,""), 
					prefs.getString(SettingsEditFragment.EXTRA_EMAIL,""), 
					prefs.getString(SettingsEditFragment.EXTRA_TIMEZONE,""));
		}
		else
			new GetCompareInfoTask(getActivity()).execute("settings.json");
		
		return rootView;
	}
	
	private void updateFields(String fn, String ln, String phone, String email, String timezone){
		tvFirstName.setText(fn);
		tvLastName.setText(ln);
		tvPhone.setText(phone);
		
		this.email = email;
		int dogIndex = email.indexOf("@");
		String emailUser = email.substring(0, dogIndex);
		String emailServer = email.substring(dogIndex, email.length());
		
		tvEmail.setText(emailUser.toUpperCase(Locale.US)+emailServer);
		
		ArrayList<String> timezones = SettingsEditFragment.getTimezoneList();
		String responseTz = timezone.replaceAll("GMT", "");
		System.out.println(responseTz);
		for (String tz : timezones) {
			if(tz.contains(responseTz)){
				tvTimezone.setText(tz);
				break;
			}
		}
		
		if(tvTimezone.getText().toString().equals(""))
			tvTimezone.setText(timezone);
		
		Editor e = prefs.edit();
		e.putString(SettingsEditFragment.EXTRA_FIRST_NAME, tvFirstName.getText().toString());
		e.putString(SettingsEditFragment.EXTRA_LAST_NAME, tvLastName.getText().toString());
		e.putString(SettingsEditFragment.EXTRA_PHONE, tvPhone.getText().toString());
		e.putString(SettingsEditFragment.EXTRA_EMAIL, tvEmail.getText().toString());
		e.putString(SettingsEditFragment.EXTRA_TIMEZONE, tvTimezone.getText().toString());
		e.putBoolean(SettingsEditFragment.PREF_JUSTSAVED, false).commit();
		e.commit();
	}
	
	class GetCompareInfoTask extends BaseRequestAsyncTask{

		public GetCompareInfoTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			llMainInfo.setVisibility(View.GONE);
			svAdditionalInfo.setVisibility(View.GONE);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);
			llMainInfo.setVisibility(View.VISIBLE);
			svAdditionalInfo.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onSuccess(JSONObject responseJSON) throws JSONException {
			
			updateFields(
					responseJSON.getString(SettingsEditFragment.EXTRA_FIRST_NAME), 
					responseJSON.getString(SettingsEditFragment.EXTRA_LAST_NAME), 
					responseJSON.getString(SettingsEditFragment.EXTRA_PHONE), 
					responseJSON.getString(SettingsEditFragment.EXTRA_EMAIL), 
					responseJSON.getString(SettingsEditFragment.EXTRA_TIMEZONE));
			
			super.onSuccess(responseJSON);
		}
	}

}
