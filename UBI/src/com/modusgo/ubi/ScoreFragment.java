package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.ScorePieChartActivity.PieChartTab;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.requesttasks.BaseRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ScoreFragment extends Fragment{
	
	private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	Vehicle vehicle;
	
	View llProgress;
	View llContent;
	TextView tvScore;
	TextView tvThisMonthMessage;
	BarGraph graph;
	
	MonthStats[] yearStats;
	
	SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		vehicle = ((DriverActivity)getActivity()).vehicle;
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(vehicle.name);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		rootView.findViewById(R.id.btnSwitchDriverMenu).setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_SWITCH_DRIVER_MENU_BUTTON_COLOR, Constants.SWITCH_DRIVER_BUTTON_BG_COLOR)));
		rootView.findViewById(R.id.bottom_line).setBackgroundColor(Color.parseColor(prefs.getString(Constants.PREF_BR_LIST_HEADER_LINE_COLOR, Constants.LIST_HEADER_LINE_COLOR)));
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(vehicle.photo == null || vehicle.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(vehicle.photo, imagePhoto, options);
	    }
		
		rootView.findViewById(R.id.btnSwitchDriverMenu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((DriverActivity)getActivity()).menu.toggle();
			}
		});

		rootView.findViewById(R.id.btnTimePeriod).setVisibility(View.GONE);
		
		llProgress = rootView.findViewById(R.id.llProgress);
		llContent = rootView.findViewById(R.id.llContent);
		tvScore = (TextView) rootView.findViewById(R.id.tvScore);
		tvThisMonthMessage = (TextView) rootView.findViewById(R.id.tvThisMonthMessage);
		graph = (BarGraph) rootView.findViewById(R.id.graph);
		
		((View)tvScore.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), EducationActivity.class);
				i.putExtra(EducationActivity.SAVED_STRING_RESOURCE, R.string.your_score);
				startActivity(i);
			}
		});
		
		rootView.findViewById(R.id.btnScoreInfo).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScoreInfoActivity.class);
				i.putExtra(VehicleEntry._ID, vehicle.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		rootView.findViewById(R.id.btnScorePieChart).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScorePieChartActivity.class);
				i.putExtra(VehicleEntry._ID, vehicle.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		rootView.findViewById(R.id.btnScoreCircles).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScoreCirclesActivity.class);
				i.putExtra(VehicleEntry._ID, vehicle.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		loadScoreGraphFromDb();
		updateScoreLabels();
		
        new GetScoreTask(getActivity().getApplicationContext()).execute("vehicles/"+vehicle.id+"/score.json");
        
		return rootView;
	}
	
	@Override
	public void onResume() {
		Utils.gaTrackScreen(getActivity(), "Score Screen");
		super.onResume();
	}
	
	private void loadScoreGraphFromDb(){
		
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.openDatabase();
		
		Cursor c = db.query(ScoreGraphEntry.TABLE_NAME, 
				new String[]{
				ScoreGraphEntry.COLUMN_NAME_MONTH,
				ScoreGraphEntry.COLUMN_NAME_SCORE,
				ScoreGraphEntry.COLUMN_NAME_GRADE}, 
				ScoreGraphEntry.COLUMN_NAME_VEHICLE_ID + " = " + vehicle.id, null, null, null, ScoreGraphEntry._ID+" ASC");
		
		yearStats = new MonthStats[12];
		for (int i = 0; i < 12; i++) {
			yearStats[i] = new MonthStats(i+1, 0, "");
		}
		
		if(c.moveToFirst()){
			int i = 0;
			while(!c.isAfterLast()){
				yearStats[i] = new MonthStats(c.getInt(0), c.getInt(1), c.getString(2));
				i++;
				c.moveToNext();
			}
		}
		c.close();
		dbHelper.closeDatabase();
		dbHelper.close();

		updateGraph();
		updateScoreLabels();
	}
	
	private void updateScoreLabels(){
		String grade = vehicle.grade.toUpperCase(Locale.getDefault());
		grade = grade.equals("NULL") || grade.equals("") ? "" : grade;
		
		if(!grade.equals("")){
			tvThisMonthMessage.setGravity(Gravity.LEFT);
			tvScore.setVisibility(View.VISIBLE);
			
			String thisMonthMessage = "This month:\n";
			tvScore.setText(grade);
			if(grade.contains("A")){
				thisMonthMessage+="Great Driving!\nKeep it up";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.score_green_light));
				tvScore.setBackgroundResource(R.drawable.circle_score_green_light);
			}
			else if(grade.contains("B")){
				thisMonthMessage+="Really Good Driving!\nKeep Going!";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.score_green_dark));
				tvScore.setBackgroundResource(R.drawable.circle_score_green_dark);
			}
			else if(grade.contains("C")){
				thisMonthMessage+="Average Driving\nYou can do better!";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.score_orange));
				tvScore.setBackgroundResource(R.drawable.circle_score_orange);
			}
			else if(grade.contains("D")){
				thisMonthMessage+="Off to a rough start.\nYou can do better!";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.score_red));
				tvScore.setBackgroundResource(R.drawable.circle_score_red);
			}
			else if(grade.contains("F")){
				thisMonthMessage+="Off to a rough start.\nNo where to go but up!";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.score_red));
				tvScore.setBackgroundResource(R.drawable.circle_score_red);
				
			}
			else{
				thisMonthMessage+="";
				tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_gray));
				tvScore.setBackgroundResource(R.drawable.circle_score_gray);
			}
			
			tvThisMonthMessage.setText(thisMonthMessage);
		}
		else{
			tvThisMonthMessage.setText("Scoring will be available soon");
			tvThisMonthMessage.setGravity(Gravity.CENTER);
			tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_gray));
			tvScore.setVisibility(View.INVISIBLE);
		}
	}
	
	private void updateGraph(){
		ArrayList<Bar> points = new ArrayList<Bar>();
//		for (int i = 0; i < 12; i++) {
//			
//			Bar d = new Bar();
//			if(i%2==0){
//				d.setColor(Color.parseColor("#99CC00"));
//				d.setValueColor(Color.parseColor("#99CC00"));
//			}
//			else{
//				d.setColor(Color.parseColor("#FFBB33"));
//				d.setValueColor(Color.parseColor("#FFBB33"));
//			}
//			
//			d.setName("Jan");
//			d.setValuePrefix("s");
//			d.setLabelColor(Color.parseColor("#697078"));
//			d.setValueString("C+");
//			d.setValue(10+10*i);
//			points.add(d);
//		}
		
		
		int maxValue = 1;
		for (int i = 0; i < 12; i++) {
			if(yearStats[i].score!=0 && !yearStats[i].grade.equals("")){
				if(maxValue<yearStats[i].score)
					maxValue = yearStats[i].score;
			}
		}
		
		for (int i = 0; i < 12; i++) {
			String month = "";
			if(yearStats[i].month-1 >= 0 && yearStats[i].month-1 < 12){
				month = months[yearStats[i].month-1];	
			}
			
			Bar b = new Bar();
			if(i<yearStats.length && yearStats[i].score!=0 && !yearStats[i].grade.equals("")){
				String grade = yearStats[i].grade;
				b.setName(month);
				b.setValue(yearStats[i].score);
				b.setValueString(grade);
				
				if(grade.contains("A")){
					b.setColor(getActivity().getResources().getColor(R.color.score_green_light));
					b.setValueColor(getActivity().getResources().getColor(R.color.score_green_light));						
				}
				else if(grade.contains("B")){
					b.setColor(getActivity().getResources().getColor(R.color.score_green_dark));
					b.setValueColor(getActivity().getResources().getColor(R.color.score_green_dark));
				}
				else if(grade.contains("C")){
					b.setColor(getActivity().getResources().getColor(R.color.score_orange));
					b.setValueColor(getActivity().getResources().getColor(R.color.score_orange));
				}
				else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
					b.setColor(getActivity().getResources().getColor(R.color.score_red));
					b.setValueColor(getActivity().getResources().getColor(R.color.score_red));
				}
				else{
					b.setColor(getActivity().getResources().getColor(R.color.ubi_gray));
					b.setValueColor(getActivity().getResources().getColor(R.color.ubi_gray));
				}
				
			}
			else{
				b.setName(month);
				b.setValue(maxValue);
				b.setValueString("");
				b.setColor(getActivity().getResources().getColor(R.color.ubi_gray));
			}
			b.setLabelColor(Color.parseColor("#697078"));
			points.add(b);
		}
		
		Typeface vTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/arialbd.ttf");
		Typeface lTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/EncodeSansNormal-300-Light.ttf");
		graph.setValueTypeface(vTypeface);
		graph.setLabelTypeface(lTypeface);
		graph.setBars(points);
		
	}
	
	private int gradeToNumber(String grade){
		switch (grade) {
		case "A+":
			return 17;
		case "A":
			return 16;
		case "A-":
			return 15;
			

		case "B+":
			return 14;
		case "B":
			return 13;
		case "B-":
			return 12;
			

		case "C+":
			return 11;
		case "C":
			return 10;
		case "C-":
			return 9;
			

		case "D+":
			return 8;
		case "D":
			return 7;
		case "D-":
			return 6;
			

		case "E+":
			return 5;
		case "E":
			return 4;
		case "E-":
			return 3;
			

		case "F+":
			return 2;
		case "F":
			return 1;
		case "F-":
			return 0;
		}
		return 0;
	}
	
	public class MonthStats {
		public int month;
		public int score;
		public String grade;
		
		public MonthStats(int month, int score, String grade) {
			super();
			this.month = month;
			this.score = score;
			this.grade = grade;
		}
	}
	
	class GetScoreTask extends BaseRequestAsyncTask{

		public GetScoreTask(Context context) {
			super(context);
		}
		
		@Override
		protected void onPreExecute() {
//			llContent.setVisibility(View.GONE);
//			llProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
	        requestParams.add(new BasicNameValuePair("page", "1"));
	        requestParams.add(new BasicNameValuePair("per_page", "1000"));
			return super.doInBackground(params);
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
//			llContent.setVisibility(View.VISIBLE);
//			llProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
		
		@Override
		protected void onError(String message) {
			super.onError(message);
		}
		
		@Override
		protected void onSuccess(JSONObject json) throws JSONException {
//			System.out.println(json);
			
			DbHelper dHelper = DbHelper.getInstance(getActivity());
			
			if(json.has("current_year_stats")){
				JSONArray yearStatsJSON = json.getJSONArray("current_year_stats");
				yearStats = new MonthStats[yearStatsJSON.length()];
				
				for (int i = 0; i < yearStats.length; i++) {
					JSONObject monthStats = yearStatsJSON.getJSONObject(i);
					yearStats[i] = new MonthStats(
							monthStats.optInt("month"),
							monthStats.optInt("score"),
							monthStats.optString("grade").equals("null") ? "" : monthStats.optString("grade"));
				}
				dHelper.saveScoreGraph(vehicle.id, yearStats);
				if(getActivity()!=null)
					loadScoreGraphFromDb();
			}
			
			dHelper.saveScoreInfo(vehicle.id, json.optString("enddate"), json.optString("startdate"), (float)json.optDouble("summary_distance"), (float)json.optDouble("summary_ead"));
			
			LinkedHashMap<String, Integer> percentageData = new LinkedHashMap<String, Integer>();
			percentageData.put("Use of speed", json.optInt("score_pace"));
			percentageData.put("Anticipation", json.optInt("score_anticipation"));
			percentageData.put("Aggression", json.optInt("score_aggression"));
			percentageData.put("Smoothness", json.optInt("score_smoothness"));
			percentageData.put("Completeness", json.optInt("score_completeness"));
			percentageData.put("Consistency", json.optInt("score_consistency"));
			dHelper.saveScorePercentage(vehicle.id, percentageData);
			
			ArrayList<PieChartTab> pcTabs = new ArrayList<PieChartTab>();
			
			float[][] pieChartsData = new float[][]{
					new float[]{
							(float)json.optDouble("timeofday0"),
							(float)json.optDouble("timeofday1"),
							(float)json.optDouble("timeofday2"),
							(float)json.optDouble("timeofday3"),
							(float)json.optDouble("timeofday4"),
							(float)json.optDouble("timeofday5"),
					},
					new float[]{
							(float)json.optDouble("roadsettings_rural"),
							(float)json.optDouble("roadsettings_suburban"),
							(float)json.optDouble("roadsettings_urban")
					},new float[]{
							(float)json.optDouble("roadtype_major"),
							(float)json.optDouble("roadtype_minor"),
							(float)json.optDouble("roadtype_local"),
							(float)json.optDouble("roadtype_trunk")
					},};
			
			pcTabs.add(new PieChartTab("TIME OF DAY", 
					pieChartsData[0],
					new String[]{
							Math.round(pieChartsData[0][0]) + "% WEEKDAY",
							Math.round(pieChartsData[0][1]) + "% WEEKDAY",
							Math.round(pieChartsData[0][2]) + "% WEEKDAY",
							Math.round(pieChartsData[0][3]) + "% WEEKDAY",
							Math.round(pieChartsData[0][4]) + "% WEEKDAY",
							Math.round(pieChartsData[0][5]) + "% WEEKEND"
					},
					new String[]{
					"12:00 AM - 6:30 AM",
					"6:30 AM - 9:30 AM",
					"9:30 AM - 4:00 PM",
					"4:00 PM - 7:00 PM",
					"7:00 PM - 11:59 PM",
					"All day"},
					new int[] {
							context.getResources().getColor(R.color.pie_black),
							context.getResources().getColor(R.color.pie_red),
							context.getResources().getColor(R.color.pie_green),
							context.getResources().getColor(R.color.pie_orange),
							context.getResources().getColor(R.color.pie_blue),
							context.getResources().getColor(R.color.pie_gray)
							}
			));
			pcTabs.add(new PieChartTab("ROAD SETTING", 
					pieChartsData[1],
					new String[]{
						Math.round(pieChartsData[1][0])+"%\nRURAL",
						Math.round(pieChartsData[1][1])+"%\nSUBURBAN",
						Math.round(pieChartsData[1][2])+"%\nURBAN"
					},
					null,
					new int[]{
							context.getResources().getColor(R.color.pie_black),
							context.getResources().getColor(R.color.pie_red),
							context.getResources().getColor(R.color.pie_green)
							}
			));
			pcTabs.add(new PieChartTab("ROAD TYPE", 
					pieChartsData[2],
					new String[]{
						Math.round(pieChartsData[2][0])+"%\nMAJOR ROAD",
		        		Math.round(pieChartsData[2][1])+"%\nMINOR ROAD",
	        			Math.round(pieChartsData[2][2])+"%\nLOCAL ROAD",
		        		Math.round(pieChartsData[2][3])+"%\nHIGHWAY"
					},
					null,
					new int[]{
							context.getResources().getColor(R.color.pie_black),
							context.getResources().getColor(R.color.pie_red),
							context.getResources().getColor(R.color.pie_green),
							context.getResources().getColor(R.color.pie_orange)
							}
			));
			dHelper.saveScorePieCharts(vehicle.id, pcTabs);
			
			if(json.has("road_env_analysis") && json.has("road_env_stats")){
				JSONObject jsonMarks = json.getJSONObject("road_env_analysis");
				JSONObject jsonStats = json.getJSONObject("road_env_stats");
				
				LinkedHashMap<String, ArrayList<CirclesSection>> circlesTabs = new LinkedHashMap<String, ArrayList<CirclesSection>>();
				circlesTabs.put("SUBURBAN", getTabSections("suburban", jsonMarks, jsonStats));
				circlesTabs.put("URBAN", getTabSections("urban", jsonMarks, jsonStats));
				circlesTabs.put("RURAL", getTabSections("rural", jsonMarks, jsonStats));
				
				dHelper.saveScoreCircles(vehicle.id, circlesTabs);
			}
			
			dHelper.close();
			
			super.onSuccess(json);
		}
		
		private ArrayList<CirclesSection> getTabSections(String pageName, JSONObject jsonMarks, JSONObject jsonStats) throws JSONException{
			JSONObject jsonMarkPage = jsonMarks.getJSONObject(pageName);
			JSONObject jsonStatsPage = jsonStats.getJSONObject(pageName);
			
			ArrayList<CirclesSection> sections = new ArrayList<CirclesSection>();
			sections.add(new CirclesSection("Use of Speed", getMarksFromJson("pace", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			sections.add(new CirclesSection("Cornering", getMarksFromJson("cornering", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			sections.add(new CirclesSection("Intersection Acceleration", getMarksFromJson("junctionacceleration", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			sections.add(new CirclesSection("Road Acceleration", getMarksFromJson("roadacceleration", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			sections.add(new CirclesSection("Intersection Braking", getMarksFromJson("junctionbrake", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			sections.add(new CirclesSection("Road Braking", getMarksFromJson("roadbrake", jsonMarkPage), getDistancesFromJson(jsonStatsPage)));
			
			return sections;
		}
		
		private int[] getMarksFromJson(String statName, JSONObject json) throws JSONException{

			JSONObject jsonUrbanHighway = json.getJSONObject("trunk");
			JSONObject jsonUrbanMajor = json.getJSONObject("major");
			JSONObject jsonUrbanMinor = json.getJSONObject("minor");
			JSONObject jsonUrbanLocal = json.getJSONObject("local");
			
			return new int[]{
					getIntOnStringScore(jsonUrbanHighway.getString(statName)),
					getIntOnStringScore(jsonUrbanMajor.getString(statName)),
					getIntOnStringScore(jsonUrbanMinor.getString(statName)),
					getIntOnStringScore(jsonUrbanLocal.getString(statName)),
					};
		}
		
		private double[] getDistancesFromJson(JSONObject json) throws JSONException{

			JSONObject jsonUrbanHighway = json.getJSONObject("trunk");
			JSONObject jsonUrbanMajor = json.getJSONObject("major");
			JSONObject jsonUrbanMinor = json.getJSONObject("minor");
			JSONObject jsonUrbanLocal = json.getJSONObject("local");
			
			return new double[]{
					jsonUrbanHighway.optDouble("distance"),
					jsonUrbanMajor.optDouble("distance"),
					jsonUrbanMinor.optDouble("distance"),
					jsonUrbanLocal.optDouble("distance"),
					};
		}
		
		private int getIntOnStringScore(String score){
			switch (score) {
			case "ideal":
				return 3;
			case "good":
			case "high":
				return 2;
			case "average":
			case "poor":
				return 1;
			case "low":
			case "unknown":
				return 0;
			default:
				return 0;
			}
		}
	}

}
