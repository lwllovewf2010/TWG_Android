package com.modusgo.ubi.jastec;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.MainActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.jastec.BluetoothCommunicator.OnConnectionListener;
import com.modusgo.ubi.utils.AnimationUtils;

public class DevicesListActivity extends MainActivity implements OnConnectionListener{

	// VehiclesAdapter driversAdapter;
	// ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	private final static int REQUEST_ENABLE_BT = 1;

	SwipeRefreshLayout lRefresh;
	ListView lvVehicles;
	TextView tvError;
	
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mDevicesArrayAdapter;
    
    private String mDeviceName;
    private String mDeviceAddress;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_devices_bt_list);
		super.onCreate(savedInstanceState);
        
		setActionBarTitle("CHOOSE DEVICE");

		lRefresh = (SwipeRefreshLayout) findViewById(R.id.lRefresh);
		lvVehicles = (ListView) findViewById(R.id.listViewDrivers);
		tvError = (TextView) findViewById(R.id.tvError);
		
		lRefresh.setColorSchemeResources(R.color.ubi_gray, R.color.ubi_green, R.color.ubi_orange, R.color.ubi_red);
		lRefresh.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				AnimationUtils.collapse(tvError);
				doDiscovery();
			}
		});
		
		mDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_devices_list_item, R.id.tvTitle);

		lvVehicles.setAdapter(mDevicesArrayAdapter);
		lvVehicles.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        //SwipeRefreshLayout setRefreshing() bug in support lib v21 workaround
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doDiscovery();
            }
        }, 1000);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(mBtAdapter != null && requestCode == REQUEST_ENABLE_BT){
			if(resultCode!=RESULT_OK){
	        	tvError.setText("Please turn Bluetooth on to continue");
				AnimationUtils.expand(tvError);
			}
			else{
				doDiscovery();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
	protected void onStart() {
		super.onStart();
		
	}
	
	private void doDiscovery() {		
		if (mBtAdapter == null) {
            // Device does not support Bluetooth
			lRefresh.setRefreshing(false);
        	tvError.setText("Your device does not support Bluetooth");
			AnimationUtils.expand(tvError);
        }
        else{
        	if (!mBtAdapter.isEnabled()) {
        		lRefresh.setRefreshing(false);
        	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	    startActivityForResult(enableBtIntent, 1);
        	}
        	else{
        		lRefresh.setRefreshing(true);
        		mDevicesArrayAdapter.clear();

                setProgressBarIndeterminateVisibility(true);

                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                }

                mBtAdapter.startDiscovery();
        	}
        }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();

            String info = ((TextView)v.findViewById(R.id.tvTitle)).getText().toString();
            String[] devinceInfo = info.split("\r\n"); 
            mDeviceName = devinceInfo[0]; 
            mDeviceAddress = devinceInfo[1];

            connect();
        }
    };

	protected void connect() 
	{
//		showDialog(VariousID.PROGRESS_DIALOG);
		BluetoothCommunicator btCommunicator = BluetoothCommunicator.getInstance();
		btCommunicator.setActivity(this);
		btCommunicator.setOnConnectionListener(this);
		
		BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(mDeviceAddress);
		btCommunicator.connect(bluetoothDevice);
	}
	
	@Override
	public void onDisconnected(Exception e) {
		System.out.println("Bt disconnected");
		
	}
	
	@Override
	public void onConnected() {
		System.out.println("Bt connected");
		
		String addressStored = prefs.getString(Constants.PREF_JASTEC_ADDRESS, "");
		
		if(!addressStored.equals(mDeviceAddress))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Constants.PREF_JASTEC_ADDRESS, mDeviceAddress);
			editor.putString(Constants.PREF_JASTEC_NAME, mDeviceName);
			editor.commit();
		}
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) 
            {
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            	mDevicesArrayAdapter.add(device.getName() + "\r\n" + device.getAddress());
            } 
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
            {
            	lRefresh.setRefreshing(false);
//                setProgressBarIndeterminateVisibility(false);
//                setTitle(R.string.devicelist_text_label_select_device);
                if (mDevicesArrayAdapter.getCount() == 0) 
                {
//                    String noDevices = getResources().getText(R.string.bt_none_found).toString();
//                    mDevicesArrayAdapter.add(noDevices);
//                    ((Activity)getApplicationContext()).setResult(Activity.RESULT_CANCELED);
//                    ((Activity)getApplicationContext()).finish();
                }
            }
        }
    };

}
