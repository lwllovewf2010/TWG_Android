package com.modusgo.ubi;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsViewFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_settings_view, null);
		
		((MainActivity)getActivity()).setActionBarTitle("SETTINGS");
		
		TextView tvFirstName = (TextView)rootView.findViewById(R.id.tvFirstName);
		TextView tvLastName = (TextView)rootView.findViewById(R.id.tvLastName);
		TextView tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
		TextView tvEmail = (TextView)rootView.findViewById(R.id.tvEmail);
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Driver d = DbHelper.getDrivers().get(prefs.getInt(Constants.PREF_CURRENT_DRIVER, 0));
		
		tvFirstName.setText(d.getFirstName());
		tvLastName.setText(d.getLastName());
		tvPhone.setText(d.phone);
		tvEmail.setText(d.email);
		imagePhoto.setImageResource(d.imageId);
		
		final SettingsEditFragment sef = new SettingsEditFragment();
		
		Button btnEdit = (Button)rootView.findViewById(R.id.btnEdit);
		btnEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.content_frame, sef)
				.addToBackStack(null)
				.commit();
			}
		});
		
		return rootView;
	}

}
