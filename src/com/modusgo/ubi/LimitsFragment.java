package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class LimitsFragment extends Fragment {

	Driver driver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(
				R.layout.fragment_limits, container, false);

		((MainActivity) getActivity()).setActionBarTitle("LIMITS");
		
		driver = DbHelper.getDrivers().get(getArguments().getInt("id", 0));
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		((ImageView)rootView.findViewById(R.id.imagePhoto)).setImageResource(driver.imageId);
		
		rootView.findViewById(R.id.btnMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);

		LinearLayout content = (LinearLayout)rootView.findViewById(R.id.llContent);
		
		ArrayList<LimitsListChild> dailyMileageLimitsChildren = new ArrayList<LimitsListChild>();
		ArrayList<LimitsListChild> timeOfDayLimitsChildren = new ArrayList<LimitsListChild>();
		ArrayList<LimitsListChild> geofenceChildren = new ArrayList<LimitsListChild>();
		dailyMileageLimitsChildren.add(new LimitsListChild(R.layout.limits_edittext_item, "Set max to"));
		timeOfDayLimitsChildren.add(new LimitsListChild(R.layout.limits_edittext_double_item, "Between", "and"));
		geofenceChildren.add(new LimitsListChild(R.layout.limits_link_item, "Set geofence"));
		
		ArrayList<LimitsListGroup> groups = new ArrayList<LimitsListGroup>();
		groups.add(new LimitsListGroup("Max speed limit"));
		groups.add(new LimitsListGroup("Daily mileage limit",dailyMileageLimitsChildren));
		groups.add(new LimitsListGroup("Harsh braking alerts"));
		groups.add(new LimitsListGroup("Time of day limits", timeOfDayLimitsChildren));
		groups.add(new LimitsListGroup("Geofence", geofenceChildren));
		groups.add(new LimitsListGroup("Low fuel"));		
		
		
		for (LimitsListGroup limitsListGroup : groups) {
			View groupView = inflater.inflate(R.layout.limits_toggle_button_item, content, false);
			
			if(limitsListGroup.childs.size()>0){
				final LinearLayout groupChilds = (LinearLayout)groupView.findViewById(R.id.llChilds);
				for (LimitsListChild child : limitsListGroup.childs) {
					final View childView = inflater.inflate(child.layoutId, groupChilds, false);

					TextView textChild = (TextView) childView.findViewById(R.id.tvTitle);
					textChild.setText(child.text[0]);
					
					switch(child.layoutId){
					case R.layout.limits_edittext_item :
						final TextView tvSingleValue = (TextView)childView.findViewById(R.id.tvValue);
						tvSingleValue.setOnClickListener(new OnClickListener() {		
							@Override
							public void onClick(View v) {
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							    // Get the layout inflater
							    LayoutInflater inflater = getActivity().getLayoutInflater();

							    // Inflate and set the layout for the dialog
							    // Pass null as the parent view because its going in the dialog layout
							    final View dialogContentView = inflater.inflate(R.layout.dialog_numbers, null);
							    ((TextView)dialogContentView.findViewById(R.id.editValue)).setText("80");
							    builder.setView(dialogContentView)
							    // Add action buttons
							    .setTitle(((TextView)childView.findViewById(R.id.tvValue)).getText())
							    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
							    	@Override
							        public void onClick(DialogInterface dialog, int id) {
							    		tvSingleValue.setText(((TextView)dialogContentView.findViewById(R.id.editValue)).getText()+" MPG");
							        }
							    })
							    .setNegativeButton("Calcel", new DialogInterface.OnClickListener() {
							    	public void onClick(DialogInterface dialog, int id) {
							    		dialog.cancel();
							        }
							    });      
							    builder.create().show();
							}
						});
						break;
					case R.layout.limits_edittext_double_item :
						if(child.text[1]!=null){
							((TextView) childView.findViewById(R.id.tvTitle2)).setText(child.text[1]);
						}
						
						final TextView tvValue = (TextView)childView.findViewById(R.id.tvValue);
						tvValue.setText(getTimeString(10, 0));
						tvValue.setOnClickListener(new OnClickListener() {		
							@Override
							public void onClick(View v) {
								TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
									@Override
									public void onTimeSet(TimePicker arg0, int hourOfDay, int minutes) {
										tvValue.setText(getTimeString(hourOfDay, minutes));
									}
								}, 10, 0, false);
								tpd.show();
							}
						});
						
						final TextView tvValue2 = (TextView)childView.findViewById(R.id.tvValue2);
						tvValue2.setText(getTimeString(11, 0));
						tvValue2.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								TimePickerDialog tpd = new TimePickerDialog(getActivity(), new OnTimeSetListener(){
									@Override
									public void onTimeSet(TimePicker arg0, int hourOfDay, int minutes) {
										tvValue2.setText(getTimeString(hourOfDay, minutes));								
									}
								}, 11, 0, false);
								tpd.show();
							}
						});
						
						break;
					case R.layout.limits_link_item :
						break;
					}
					
					groupChilds.addView(childView);
				}
				((ToggleButton)groupView.findViewById(R.id.btnToggle)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked)
							groupChilds.setVisibility(View.VISIBLE);
						else
							groupChilds.setVisibility(View.GONE);
					}
				});
			}
			
			((TextView)groupView.findViewById(R.id.tvTitle)).setText(limitsListGroup.groupTitle);
			
			content.addView(groupView);
		}
		
		
		

		return rootView;
	}
	
	private String getTimeString(int hourOfDay, int minutes){
		Calendar datetime = Calendar.getInstance();
	    datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
	    datetime.set(Calendar.MINUTE, minutes);

	    String am_pm ="";
	    if (datetime.get(Calendar.AM_PM) == Calendar.AM)
	        am_pm = "AM";
	    else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
	        am_pm = "PM";

	    String strHrsToShow = datetime.get(Calendar.HOUR)<10 ? "0"+datetime.get(Calendar.HOUR) : ""+datetime.get(Calendar.HOUR);
	    String strMntToShow = datetime.get(Calendar.MINUTE)<10 ? "0"+datetime.get(Calendar.MINUTE) : ""+datetime.get(Calendar.MINUTE); 

	    return strHrsToShow+":"+strMntToShow+" "+am_pm;
	}

	class LimitsListGroup{
		String groupTitle;
		boolean enabled = false;
		ArrayList<LimitsListChild> childs;
		
		public LimitsListGroup(String groupTitle,
				ArrayList<LimitsListChild> childs) {
			super();
			this.groupTitle = groupTitle;
			this.childs = childs;
		}
		public LimitsListGroup(String groupTitle) {
			super();
			this.groupTitle = groupTitle;
			this.childs = new ArrayList<LimitsListChild>();
		}
	}
	
	class LimitsListChild{
		int layoutId;
		String text[];
		
		public LimitsListChild(int layoutId, String... text) {
			super();
			this.layoutId = layoutId;
			this.text = text;
		}
	}
}
