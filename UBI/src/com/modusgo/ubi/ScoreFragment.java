package com.modusgo.ubi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.modusgo.demo.R;
import com.modusgo.ubi.ScoreCirclesActivity.CirclesSection;
import com.modusgo.ubi.ScorePieChartActivity.PieChartTab;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.db.ScoreGraphContract.ScoreGraphEntry;
import com.modusgo.ubi.db.VehicleContract.VehicleEntry;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ScoreFragment extends Fragment{
	
	private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	
	Driver driver;
	
	View llProgress;
	View llContent;
	TextView tvScore;
	TextView tvThisMonthMessage;
	TextView tvLastMonthMessage;
	ImageView imageLastMonthArrow;
	BarGraph graph;
	
	MonthStats[] yearStats;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment_score, container, false);
		
		((MainActivity)getActivity()).setActionBarTitle("SCORE");
		
		driver = ((DriverActivity)getActivity()).driver;
		
		((TextView)rootView.findViewById(R.id.tvName)).setText(driver.name);
		
		ImageView imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
	    if(driver.photo == null || driver.photo.equals(""))
	    	imagePhoto.setImageResource(R.drawable.person_placeholder);
	    else{
	    	DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	    	
	    	ImageLoader.getInstance().displayImage(driver.photo, imagePhoto, options);
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
		tvLastMonthMessage = (TextView) rootView.findViewById(R.id.tvLastMonthMessage);
		imageLastMonthArrow = (ImageView) rootView.findViewById(R.id.imageLastMonthArrow);
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
				i.putExtra(VehicleEntry._ID, driver.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		rootView.findViewById(R.id.btnScorePieChart).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScorePieChartActivity.class);
				i.putExtra(VehicleEntry._ID, driver.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		rootView.findViewById(R.id.btnScoreCircles).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScoreCirclesActivity.class);
				i.putExtra(VehicleEntry._ID, driver.id);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.flip_in,R.anim.flip_out);
			}
		});
		
		loadScoreGraphFromDb();
		updateScoreLabels();
		
        new GetScoreTask(getActivity()).execute("vehicles/"+driver.id+"/score.json");
        
		return rootView;
	}
	
	private void loadScoreGraphFromDb(){
		
		DbHelper dbHelper = DbHelper.getInstance(getActivity());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		Cursor c = db.query(ScoreGraphEntry.TABLE_NAME, 
				new String[]{
				ScoreGraphEntry.COLUMN_NAME_MONTH,
				ScoreGraphEntry.COLUMN_NAME_SCORE,
				ScoreGraphEntry.COLUMN_NAME_GRADE}, 
				ScoreGraphEntry.COLUMN_NAME_DRIVER_ID + " = " + driver.id, null, null, null, ScoreGraphEntry.COLUMN_NAME_MONTH+" ASC");
		
		yearStats = new MonthStats[12];
		for (int i = 0; i < 12; i++) {
			yearStats[i] = new MonthStats(i, 0, "");
		}
		
		if(c.moveToFirst()){
			while(!c.isAfterLast()){
				int month = c.getInt(0);
				yearStats[month-1] = new MonthStats(month, c.getInt(1), c.getString(2));
				c.moveToNext();
			}
		}
		c.close();
		db.close();
		dbHelper.close();

		updateGraph();
	}
	
	private void updateScoreLabels(){
		String grade = driver.grade.toUpperCase(Locale.getDefault());
		grade = grade.equals("NULL") || grade.equals("") ? "X" : grade;
		String thisMonthMessage = "This month:\n";
		tvScore.setText(grade);
		if(grade.contains("A") || grade.contains("B")){
			thisMonthMessage+="Great Score!\nKeep it up!";
			tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_green));
			tvScore.setBackgroundResource(R.drawable.circle_score_green);
		}
		else if(grade.contains("C")){
			thisMonthMessage+="Average Score\nYou can do better!";
			tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_orange));
			tvScore.setBackgroundResource(R.drawable.circle_score_orange);
		}
		else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
			thisMonthMessage+="Hmm Not good\nYou can do better!";
			tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_red));
			tvScore.setBackgroundResource(R.drawable.circle_score_red);
		}
		else{
			thisMonthMessage+="";
			tvThisMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_gray));
			tvScore.setBackgroundResource(R.drawable.circle_score_gray);
		}
		
		tvThisMonthMessage.setText(thisMonthMessage);
		
		Calendar c = Calendar.getInstance();
		int currentMonth = c.get(Calendar.MONTH);
		String lastMonthGrade = yearStats[currentMonth-1].grade;
		
		if(!lastMonthGrade.equals("") && !grade.equals("X")){
			tvLastMonthMessage.setText(lastMonthGrade+" Last Month");
			if(gradeToNumber(grade)>gradeToNumber(lastMonthGrade)){
				tvLastMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_green));
				imageLastMonthArrow.setImageResource(R.drawable.arrow_up_green);
			}
			else{
				tvLastMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_red));
				imageLastMonthArrow.setImageResource(R.drawable.arrow_down_red);				
			}
		}
		else{
			tvLastMonthMessage.setText("N/A Last Month");
			tvLastMonthMessage.setTextColor(getActivity().getResources().getColor(R.color.ubi_gray));
			imageLastMonthArrow.setVisibility(View.INVISIBLE);
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
		
		
		int startIndex = -1;
		int maxValue = 1;
		for (int i = 0; i < 12; i++) {
			if(yearStats[i].score!=0 && !yearStats[i].grade.equals("")){
				if(startIndex==-1)
					startIndex = i;
				if(maxValue<yearStats[i].score)
					maxValue = yearStats[i].score;
			}
		}
		if(startIndex==-1)
			startIndex = 0;
		
		for (int i = startIndex; i < startIndex+12; i++) {
			String month;
			if(i>=12)
				month = months[i-12];
			else
				month = months[i];
			
			Bar b = new Bar();
			if(i<yearStats.length && yearStats[i].score!=0 && !yearStats[i].grade.equals("")){
				String grade = yearStats[i].grade;
				b.setName(month);
				b.setValue(yearStats[i].score);
				b.setValueString(grade);
				
				if(grade.contains("A") || grade.contains("B")){
					b.setColor(getActivity().getResources().getColor(R.color.ubi_green));
					b.setValueColor(getActivity().getResources().getColor(R.color.ubi_green));						
				}
				else if(grade.contains("C")){
					b.setColor(getActivity().getResources().getColor(R.color.ubi_orange));
					b.setValueColor(getActivity().getResources().getColor(R.color.ubi_orange));
				}
				else if(grade.contains("D") || grade.contains("E") || grade.contains("F")){
					b.setColor(getActivity().getResources().getColor(R.color.ubi_red));
					b.setValueColor(getActivity().getResources().getColor(R.color.ubi_red));
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
			llContent.setVisibility(View.GONE);
			llProgress.setVisibility(View.VISIBLE);
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
			llContent.setVisibility(View.VISIBLE);
			llProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
		
		@Override
		protected void onError(String message) {
			try{
				onSuccess(Utils.getJSONObjectFromAssets(getActivity(), "score.json"));
			}
			catch(JSONException e){
				e.printStackTrace();
			}
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
				dHelper.saveScoreGraph(driver.id, yearStats);
				updateGraph();
			}
			
			LinkedHashMap<String, Integer> percentageData = new LinkedHashMap<String, Integer>();
			percentageData.put("Use of speed", json.optInt("score_pace"));
			percentageData.put("Anticipation", json.optInt("score_anticipation"));
			percentageData.put("Aggression", json.optInt("score_aggression"));
			percentageData.put("Smoothness", json.optInt("score_smoothness"));
			percentageData.put("Completeness", json.optInt("score_completeness"));
			percentageData.put("Consistency", json.optInt("score_consistency"));
			dHelper.saveScorePercentage(driver.id, percentageData);
			
			ArrayList<PieChartTab> pcTabs = new ArrayList<PieChartTab>();
			
			float[][] pieChartsData = new float[][]{
					new float[]{
							(float)json.optDouble("timeofday0"),
							(float)json.optDouble("timeofday1"),
							(float)json.optDouble("timeofday2"),
							(float)json.optDouble("timeofday3"),
							(float)json.optDouble("timeofday4"),
							(float)json.optDouble("timeofday5")
					},
					new float[]{
							(float)json.optDouble("roadsettings_rural"),
							(float)json.optDouble("roadsettings_suburban"),
							(float)json.optDouble("roadsettings_urban")
					},new float[]{
							(float)json.optDouble("roadtype_major"),
							(float)json.optDouble("roadtype_local"),
							(float)json.optDouble("roadtype_trunk"),
							(float)json.optDouble("roadtype_minor")
					},};
			
			pcTabs.add(new PieChartTab("TIME OF DAY", 
					pieChartsData[0],
					new String[]{
						Math.round(pieChartsData[0][0])+"% WEEKDAY",
			        	Math.round(pieChartsData[0][1])+"% WEEKDAY",
			        	Math.round(pieChartsData[0][2])+"% WEEKEND",
			        	Math.round(pieChartsData[0][3])+"% WEEKDAY",
			        	Math.round(pieChartsData[0][4])+"% WEEKDAY",
			        	Math.round(pieChartsData[0][5])+"% WEEKDAY"
					},
					new String[]{"6:30 AM - 9:30 AM","4:00 PM - 7:00 PM","All day","9:30 AM - 4:00 PM","7:00 PM - 11:59 PM","12:00 AM - 6:30 AM"}
			));
			pcTabs.add(new PieChartTab("ROAD SETTING", 
					pieChartsData[1],
					new String[]{
						Math.round(pieChartsData[1][0])+"%\nRURAL",
						Math.round(pieChartsData[1][1])+"%\nSUBURBAN",
						Math.round(pieChartsData[1][2])+"%\nURBAN"
					},
					null
			));
			pcTabs.add(new PieChartTab("ROAD TYPE", 
					pieChartsData[2],
					new String[]{
						Math.round(pieChartsData[2][0])+"%\nMAJOR ROAD",
		        		Math.round(pieChartsData[2][1])+"%\nLOCAL ROAD",
		        		Math.round(pieChartsData[2][2])+"%\nHIGHWAY",
		        		Math.round(pieChartsData[2][3])+"%\nMINOR ROAD"
					},
					null
			));
			dHelper.saveScorePieCharts(driver.id, pcTabs);
			
			if(json.has("road_env_analysis") && json.has("road_env_stats")){
				JSONObject jsonMarks = json.getJSONObject("road_env_analysis");
				JSONObject jsonStats = json.getJSONObject("road_env_stats");
				
				LinkedHashMap<String, ArrayList<CirclesSection>> circlesTabs = new LinkedHashMap<String, ArrayList<CirclesSection>>();
				circlesTabs.put("SUBURBAN", getTabSections("suburban", jsonMarks, jsonStats));
				circlesTabs.put("URBAN", getTabSections("urban", jsonMarks, jsonStats));
				circlesTabs.put("RURAL", getTabSections("rural", jsonMarks, jsonStats));
				
				dHelper.saveScoreCircles(driver.id, circlesTabs);
			}
			
			dHelper.close();
			
			super.onSuccess(json);
		}
		
		private ArrayList<CirclesSection> getTabSections(String pageName, JSONObject jsonMarks, JSONObject jsonStats) throws JSONException{
			JSONObject jsonMarkPage = jsonMarks.getJSONObject(pageName);
			JSONObject jsonStatsPage = jsonStats.getJSONObject(pageName);
			
			ArrayList<CirclesSection> sections = new ArrayList<CirclesSection>();
			sections.add(new CirclesSection("Use of Speed", getMarksFromJson("pace", jsonMarkPage), getDistancesFromJson("pace", jsonStatsPage)));
			sections.add(new CirclesSection("Cornering", getMarksFromJson("cornering", jsonMarkPage), getDistancesFromJson("cornering", jsonStatsPage)));
			sections.add(new CirclesSection("Intersection Acceleration", getMarksFromJson("junctionacceleration", jsonMarkPage), getDistancesFromJson("junctionacceleration", jsonStatsPage)));
			sections.add(new CirclesSection("Road Acceleration", getMarksFromJson("pace", jsonMarkPage), getDistancesFromJson("roadacceleration", jsonStatsPage)));
			sections.add(new CirclesSection("Intersection Braking", getMarksFromJson("junctionbrake", jsonMarkPage), getDistancesFromJson("junctionbrake", jsonStatsPage)));
			sections.add(new CirclesSection("Road Braking", getMarksFromJson("roadbrake", jsonMarkPage), getDistancesFromJson("roadbrake", jsonStatsPage)));
			
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
		
		private double[] getDistancesFromJson(String statName, JSONObject json) throws JSONException{

			JSONObject jsonUrbanHighway = json.getJSONObject("trunk");
			JSONObject jsonUrbanMajor = json.getJSONObject("major");
			JSONObject jsonUrbanMinor = json.getJSONObject("minor");
			JSONObject jsonUrbanLocal = json.getJSONObject("local");
			
			return new double[]{
					jsonUrbanHighway.optDouble(statName),
					jsonUrbanMajor.optDouble(statName),
					jsonUrbanMinor.optDouble(statName),
					jsonUrbanLocal.optDouble(statName),
					};
		}
		
		private int getIntOnStringScore(String score){
			switch (score) {
			case "ideal":
				return 3;
			case "average":
				return 2;
			case "high":
				return 1;
			case "unknown":
				return 0;
			default:
				return 0;
			}
		}
	}

}
