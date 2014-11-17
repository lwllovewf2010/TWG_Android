package com.modusgo.ubi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.FileUtils;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.modusgo.ubi.db.DbHelper;
import com.modusgo.ubi.requesttasks.BasePostRequestAsyncTask;
import com.modusgo.ubi.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsEditFragment extends Fragment implements ImageChooserListener{

	private static final String TAG_CHANGED = "changed";
	private static final String PHOTOS_FOLDER = "ubi_avatars";

	ImageView imagePhoto;
	ImageView imagePickPhoto;
	EditText editFirstName;
	EditText editLastName;
	EditText editPhone;
	EditText editEmail;
	Spinner spinnerTimezone;
	Spinner spinnerCar;
	EditText editPassword;
	Button btnUpdate;
	Button btnCancel;
	
	private DisplayImageOptions imageLoaderOptions;
	private ImageChooserManager imageChooserManager;
	private String filePath="";
	private int chooserType;
	private String imageBase64="";
	
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

		imagePhoto = (ImageView)rootView.findViewById(R.id.imagePhoto);
		imagePickPhoto = (ImageView)rootView.findViewById(R.id.imagePickPhoto);
		editFirstName = (EditText)rootView.findViewById(R.id.editFirstName);
		editLastName = (EditText)rootView.findViewById(R.id.editLastName);
		editPhone = (EditText)rootView.findViewById(R.id.editPhone);
		editEmail = (EditText)rootView.findViewById(R.id.editEmail);
		spinnerTimezone = (Spinner)rootView.findViewById(R.id.spinnerTimezone);
		spinnerCar = (Spinner)rootView.findViewById(R.id.spinnerCar);
		editPassword = (EditText)rootView.findViewById(R.id.editPassword);
		final EditText editConfirmPassword = (EditText)rootView.findViewById(R.id.editConfirmPassword);
		final TextView tvPasswordError = (TextView)rootView.findViewById(R.id.tvPasswordError);
		btnUpdate = (Button)rootView.findViewById(R.id.btnUpdate);
		btnCancel = (Button)rootView.findViewById(R.id.btnCancel);
		
		editFirstName.setText(prefs.getString(Constants.PREF_FIRST_NAME, ""));
		editLastName.setText(prefs.getString(Constants.PREF_LAST_NAME, ""));
		editPhone.setText(prefs.getString(Constants.PREF_PHONE, ""));
		editEmail.setText(prefs.getString(Constants.PREF_EMAIL, ""));
		
		if(filePath.equals("")){
			
			System.out.println("file path empty");
			String photoURL = prefs.getString(Constants.PREF_PHOTO, "");
			
			imageLoaderOptions = new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.person_placeholder)
	        .showImageForEmptyUri(R.drawable.person_placeholder)
	        .showImageOnFail(R.drawable.person_placeholder)
	        .cacheInMemory(true)
	        .cacheOnDisk(true)
	        .build();
	
	    	ImageLoader.getInstance().displayImage(photoURL, imagePhoto, imageLoaderOptions);
		}
		
		imagePickPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    builder.setTitle("Pick photo")
			           .setItems(new String[]{"From galery","Make a photo"}, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   switch (which) {
			            	   case 0:
			            		   chooseImage();
			            		   break;
			            	   case 1:
			            		   takePicture();
			            		   break;
			            	   default:
			            		   chooseImage();
			            		   break;
			            	   }
			           }
			    });
			    builder.create().show();
			}
		});
		imagePickPhoto.setOnTouchListener(new OnTouchListener() {
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

		btnUpdate.setBackgroundDrawable(Utils.getButtonBgStateListDrawable(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
		try{
			btnUpdate.setTextColor(Color.parseColor(prefs.getString(Constants.PREF_BR_BUTTONS_TEXT_COLOR, Constants.BUTTON_TEXT_COLOR)));
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
			btnCancel.setTextColor(Utils.getButtonTextColorStateList(prefs.getString(Constants.PREF_BR_BUTTONS_BG_COLOR, Constants.BUTTON_BG_COLOR)));
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
	
	@Override
	public void onResume() {
		Utils.gaTrackScreen(getActivity(), "Settings Edit Screen");
		super.onResume();
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
		        
		        if(!imageBase64.equals("")){
		        	requestParams.add(new BasicNameValuePair("photo", imageBase64));
		        }
		        
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
			
			if(!imageBase64.equals("")){
				ImageLoader.getInstance().clearDiskCache();
				ImageLoader.getInstance().clearMemoryCache();
			}
			
			try{
				getActivity().getSupportFragmentManager().popBackStack();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			super.onSuccess(responseJSON);
		}
	}
	
	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 160;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
               || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);

    }

	private void chooseImage() {
		chooserType = ChooserType.REQUEST_PICK_PICTURE;
		imageChooserManager = new ImageChooserManager(this,	ChooserType.REQUEST_PICK_PICTURE, PHOTOS_FOLDER, false);
		imageChooserManager.setImageChooserListener(this);
		try {
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_CAPTURE_PICTURE, PHOTOS_FOLDER, false);
		imageChooserManager.setImageChooserListener(this);
		try {
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK
				&& (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			imageChooserManager.submit(requestCode, data);
		}
	}

	@Override
	public void onImageChosen(final ChosenImage image) {
		if(getActivity()!=null && image!=null){
			Bitmap bmp = null;
			try{
				bmp = ThumbnailUtils.extractThumbnail(decodeUri(Uri.parse("file://"+image.getFilePathOriginal())), 160, 160, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
				imageBase64 = Utils.encodeTobase64(bmp);
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
				Toast.makeText(getActivity(), "Something gone wrong", Toast.LENGTH_LONG).show();
			}
			final Bitmap bitmap = bmp;
			
			File photos_dir = new File(FileUtils.getDirectory(PHOTOS_FOLDER));
			if (photos_dir.isDirectory()) {
		        String[] children = photos_dir.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(photos_dir, children[i]).delete();
		        }
		    }
			photos_dir.delete();
			
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (bitmap != null) {
						imagePhoto.setImageBitmap(bitmap);
						ImageLoader.getInstance().cancelDisplayTask(imagePhoto);				
					}
					else{
						Toast.makeText(getActivity(), "Something gone wrong", Toast.LENGTH_LONG).show();
					}				
				}
			});
		}
	}

	@Override
	public void onError(final String reason) {
		if(getActivity()!=null){
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	// Should be called if for some reason the ImageChooserManager is null (Due
	// to destroying of activity for low memory situations)
	private void reinitializeImageChooser() {
		imageChooserManager = new ImageChooserManager(this, chooserType, PHOTOS_FOLDER, false);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("chooser_type", chooserType);
		outState.putString("media_path", filePath);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("chooser_type")) {
				chooserType = savedInstanceState.getInt("chooser_type");
			}

			if (savedInstanceState.containsKey("media_path")) {
				filePath = savedInstanceState.getString("media_path");
			}
		}
		super.onActivityCreated(savedInstanceState);
	}
}
