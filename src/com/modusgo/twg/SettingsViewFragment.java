package com.modusgo.twg;

import java.util.Locale;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

import com.modusgo.twg.R;
import com.modusgo.twg.db.DbHelper;
import com.modusgo.twg.utils.Utils;
import com.modusgo.twg.utils.Vehicle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsViewFragment extends Fragment{	
	
	ProgressBar progress;
	LinearLayout llMainInfo;
	ScrollView svAdditionalInfo;
	
	TextView tvFirstName;
	TextView tvLastName;
	TextView tvPhone;
	TextView tvEmail;
	TextView tvTimezone;
	TextView tvCar;
	ImageView imagePhoto;
	
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
		tvCar = (TextView)rootView.findViewById(R.id.tvCar);
		imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		
		tvTimezone.setText("");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		imagePhoto.setImageResource(R.drawable.person_placeholder);		
		
		Button btnEdit = (Button)rootView.findViewById(R.id.btnEdit);
		btnEdit.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
		try{
			btnEdit.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
		}
	    catch(Exception e){
	    	e.printStackTrace();
	    }
		btnEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.content_frame, new SettingsEditFragment())
				.addToBackStack(null)
				.commit();
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		updateFields();
		Utils.gaTrackScreen(getActivity(), "Settings View Screen");
		super.onResume();
	}
	
	private void updateFields(){
		tvFirstName.setText(prefs.getString(Constants.PREF_FIRST_NAME, ""));
		tvLastName.setText(prefs.getString(Constants.PREF_LAST_NAME, ""));
		tvPhone.setText(prefs.getString(Constants.PREF_PHONE, ""));
		long vehicleId = prefs.getLong(Constants.PREF_VEHICLE_ID, -1);
		if(vehicleId!=-1){
			DbHelper dbHelper = DbHelper.getInstance(getActivity());
			Vehicle v = dbHelper.getVehicle(vehicleId);
			dbHelper.close();
			tvCar.setText(v.getCarFullName());
		}
		else{
			tvCar.setText("N/A");			
		}
		
		email = prefs.getString(Constants.PREF_EMAIL, "");
		if(email!=null && email.length()>0){
			int dogIndex = email.indexOf("@");
			String emailUser = email.substring(0, dogIndex);
			String emailServer = email.substring(dogIndex, email.length());
			
			tvEmail.setText(emailUser.toUpperCase(Locale.US)+emailServer);
		}
		else
			tvEmail.setText("N/A");
		
		tvTimezone.setText(prefs.getString(Constants.PREF_TIMEZONE, ""));
		
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
	}
}
