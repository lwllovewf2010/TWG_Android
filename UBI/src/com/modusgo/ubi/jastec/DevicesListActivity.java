package com.modusgo.ubi.jastec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.modusgo.ubi.Constants;
import com.modusgo.ubi.MainActivity;
import com.modusgo.ubi.R;
import com.modusgo.ubi.jastec.BluetoothCommunicator.OnConnectionListener;
import com.modusgo.ubi.jastec.BluetoothCommunicator.OnDataListener;
import com.modusgo.ubi.jastec.packet.DrivingBroadcastingAfterStart;
import com.modusgo.ubi.jastec.packet.DrivingBroadcastingCurrent;
import com.modusgo.ubi.jastec.packet.DrivingSupportWhole;
import com.modusgo.ubi.jastec.packet.FirmwareVersion;
import com.modusgo.ubi.jastec.packet.GetProtocol;
import com.modusgo.ubi.jastec.packet.ReadDBSuccess;
import com.modusgo.ubi.jastec.packet.SensorBroadcating;
import com.modusgo.ubi.jastec.packet.SensorSupportWhole;
import com.modusgo.ubi.jastec.packet.SerialNumberSuccess;
import com.modusgo.ubi.utils.AnimationUtils;

public class DevicesListActivity extends MainActivity implements OnConnectionListener, OnDataListener{

	// VehiclesAdapter driversAdapter;
	// ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	private final static int REQUEST_ENABLE_BT = 1;
	private final static String TAG = "Jastec";

	SwipeRefreshLayout lRefresh;
	ListView lvDevices;
	TextView tvError;
	
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mDevicesArrayAdapter;
    
    private String mDeviceName;
    private String mDeviceAddress;
    ProgressDialog progressDialog;

	protected Map<Protocol.PacketType, IPacketProcessor> mMapProcessor;
	
	private DrivingBroadcastingCurrent mDrivingBroadcastingCurrent;
	private DrivingBroadcastingAfterStart mDrivingBroadcastingAfterStart;
	private SensorBroadcating mSensorBroadcating;
	
	private Dialog mAlertDialog1ButtonNormal;
	private ProgressDialog mDialogPreviewInit;
	
	private Timer mTimer;
	private long mCurrentTime;
	private int mProtocolNum;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_devices_bt_list);
		super.onCreate(savedInstanceState);
        
		setActionBarTitle("CHOOSE DEVICE");

		lRefresh = (SwipeRefreshLayout) findViewById(R.id.lRefresh);
		lvDevices = (ListView) findViewById(R.id.listViewDevices);
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

		lvDevices.setAdapter(mDevicesArrayAdapter);
		lvDevices.setOnItemClickListener(mDeviceClickListener);

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
        
        findViewById(R.id.btnStart).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(BluetoothCommunicator.getInstance().isConnected())
				{
					byte[] getProtocolPacket = createProtocolPacket();
					try {
						BluetoothCommunicator.getInstance().write(getProtocolPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
        
        findViewById(R.id.btnStop).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mTimer != null)
					mTimer.cancel();
				
				if(!BluetoothCommunicator.getInstance().isConnected())
					return;
				
				byte[] drivingStopPacket = createDrivingStop();
				byte[] sensorStopPacket = createSensorStop();
				try {
					BluetoothCommunicator.getInstance().write(drivingStopPacket);
					BluetoothCommunicator.getInstance().write(sensorStopPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        
        mMapProcessor = new HashMap<Protocol.PacketType, IPacketProcessor>();
        
        mMapProcessor.put(Protocol.PacketType.READ_SERIAL_NUMBER_SUCCESS, 			new OnReadSerialNumberSuccess());
		mMapProcessor.put(Protocol.PacketType.READ_SERIAL_NUMBER_INVALID, 			new OnReadSerialNumberInvalid());
		mMapProcessor.put(Protocol.PacketType.FIRMWARE_VERSION, 					new OnFirmwareVersion());
		mMapProcessor.put(Protocol.PacketType.READ_DB_SUCCESS,	 					new OnReadDBSuccess());
		mMapProcessor.put(Protocol.PacketType.READ_DB_ERROR,	 					new OnReadDBError());
		mMapProcessor.put(Protocol.PacketType.BLUETOOTH_DEVICE_NAME,	 			new OnBluetoothDeviceName());
		mMapProcessor.put(Protocol.PacketType.BLUETOOTH_PASSWORD,		 			new OnBluetoothPassword());
		mMapProcessor.put(Protocol.PacketType.SET_VEHICLE_INFO,			 			new OnSetVehicleInfo());
		mMapProcessor.put(Protocol.PacketType.MONITORING_STOP,			 			new OnMonitoringStop());
		mMapProcessor.put(Protocol.PacketType.EXTERNAL_BLOCK_ERASE_COMPLETE,	 	new OnExternalBlockEraseComplete());
		mMapProcessor.put(Protocol.PacketType.EXTERNAL_WRITE_COMPLETE,			 	new OnExternalWriteComplete());
		mMapProcessor.put(Protocol.PacketType.HW_RESET,			 					new OnHWReset());
		mMapProcessor.put(Protocol.PacketType.BLUETOOTH_PINCODE, 					new OnBluetoothPinCode());
		
		mMapProcessor.put(Protocol.PacketType.GET_PROTOCOL, 					new OnGetProtocol());
		mMapProcessor.put(Protocol.PacketType.DRIVING_SUPPORT_WHOLE, 			new OnDrivingSupportWhole());
		mMapProcessor.put(Protocol.PacketType.SENSOR_SUPPORT_WHOLE, 			new OnSensorSupportWhole());
		
		mMapProcessor.put(Protocol.PacketType.DRIVING_BROADCASTING_CURRENT, 	new OnDrivingBroadcastingCurrent());
		mMapProcessor.put(Protocol.PacketType.DRIVING_BROADCASTING_AFTER_START, new OnDrivingBroadcastingAfterStart());
		mMapProcessor.put(Protocol.PacketType.SENSOR_BROADCASTING, 				new OnSensorBroadcating());
		
		mMapProcessor.put(Protocol.PacketType.SENSOR_SINGLE, 					new OnSensorSingle());
		mMapProcessor.put(Protocol.PacketType.DRIVING_SINGLE, 					new OnDrivingSingle());
		
	}
	
	private byte[] createProtocolPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x00);
		PacketBuilder.getInstance().putSubID((byte)0x20);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createDrivingStop()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x30);
		PacketBuilder.getInstance().putSubID((byte)0x30);
	    
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createSensorStop()
	{
		PacketBuilder.getInstance().init((short)4);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x20);
		PacketBuilder.getInstance().putSubID((byte)0x30);
		byte[] data = new byte[]{ 0x00, 0x0D, 0x00, 0x00 };
		PacketBuilder.getInstance().putDataBlock(data);
		
	    return PacketBuilder.getInstance().buildOnBT();
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

    		progressDialog = ProgressDialog.show(DevicesListActivity.this, "Processing", "Please, wait...");
    		
            connect();
        }
    };

	protected void connect() 
	{
//		showDialog(VariousID.PROGRESS_DIALOG);
		BluetoothCommunicator btCommunicator = BluetoothCommunicator.getInstance();
		btCommunicator.setActivity(this);
		btCommunicator.setOnConnectionListener(this);
		btCommunicator.setOnDataListener(this);
		
		BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(mDeviceAddress);
		btCommunicator.connect(bluetoothDevice);
	}
	
	@Override
	public void onDisconnected(Exception e) {
		System.out.println("Bt disconnected");
		Toast.makeText(this, "Bt disconnected", Toast.LENGTH_SHORT).show();
		progressDialog.dismiss();
		
	}
	
	@Override
	public void onConnected() {
		System.out.println("Bt connected");
		Toast.makeText(this, "Bt connected", Toast.LENGTH_SHORT).show();
		
		String addressStored = prefs.getString(Constants.PREF_JASTEC_ADDRESS, "");
		
//		if(!addressStored.equals(mDeviceAddress))
//		{
//			SharedPreferences.Editor editor = prefs.edit();
//			editor.putString(Constants.PREF_JASTEC_ADDRESS, mDeviceAddress);
//			editor.putString(Constants.PREF_JASTEC_NAME, mDeviceName);
//			editor.commit();
//		}
		progressDialog.dismiss();
		packets = 0;
		//startActivity(new Intent(this, HomeActivity.class));
		//finish();
	}
	
	int packets = 0;
	
	public void onReceived(Byte[][] packets)
	{
		System.out.println("packet received");
		for(int i = 0;i < packets.length; i++)
		{
			PacketParser.getInstance().init(packets[i]);
			Protocol.PacketType type = PacketParser.getInstance().getPacketType();

			Toast.makeText(this, "packet received type = "+type+", count = "+packets.length, Toast.LENGTH_SHORT).show();
//			Log.e(TAG, "type : " + type);
//			Log.e(TAG, PacketParser.getInstance().getLog());
			
			IPacketProcessor packetProcessor = mMapProcessor.get(type);
			if(packetProcessor == null)
			{
				Toast.makeText(getApplicationContext(), "do not exist the type("+type+")", Toast.LENGTH_LONG).show();
				continue;
			}
			
			if(!packetProcessor.onTransFunc(packets[i]))
			{
				Toast.makeText(getApplicationContext(), "do not proceed the type("+type+")", Toast.LENGTH_LONG).show();
				continue;
			}
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
    
    private class OnReadSerialNumberSuccess implements IPacketProcessor
	{
		private SerialNumberSuccess mSerialNumber;
		public OnReadSerialNumberSuccess()
		{
			mSerialNumber = new SerialNumberSuccess();
		}
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mSerialNumber.parse(packet);
			
//			SharedPreferences.Editor editor = getSharedPreferences(PrefInfo.NAME, Activity.MODE_PRIVATE).edit();
//			editor.putString(PrefInfo.CONFIG_SERIAL_NUMBER, mSerialNumber.SerialNumber);
//			editor.commit();
			
			PacketBuilder.getInstance().init((short)0);
			PacketBuilder.getInstance().putTar((byte)0xA0);
			PacketBuilder.getInstance().putID((byte)0x10);
			PacketBuilder.getInstance().putSubID((byte)0x10);
			byte[] firmwarePacket = PacketBuilder.getInstance().buildOnBT();
			
			try {
				BluetoothCommunicator.getInstance().write(firmwarePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	private class OnReadSerialNumberInvalid implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			return true;
		}
	}
	
	private class OnFirmwareVersion implements IPacketProcessor
	{
		private FirmwareVersion mFirmwareVersion;
		public OnFirmwareVersion()
		{
			mFirmwareVersion = new FirmwareVersion();
		}
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mFirmwareVersion.parse(packet);
//			SharedPreferences.Editor editor = getSharedPreferences(PrefInfo.NAME, Activity.MODE_PRIVATE).edit();
//			editor.putString(PrefInfo.CONFIG_FIRMWARE_VERSION, mFirmwareVersion.FirmwareVersion);
//			editor.putBoolean(PrefInfo.CONFIG_DEVICE_INIT, true);
//			mDidDeviceInit = true;
//			editor.commit();
//			
//			mDialogInit.dismiss();
//			removeDialog(VariousID.PROGRESS_INIT_CONFIGURATION);
			
			return true;
		}
	}
	
	private class OnReadDBSuccess implements IPacketProcessor
	{
		private ReadDBSuccess mReadDBSuccess;
		public OnReadDBSuccess()
		{
			mReadDBSuccess = new ReadDBSuccess();
		}
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mReadDBSuccess.parse(packet);
			
			
			return true;
		}
	}
	
	private class OnReadDBError implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			return true;
		}
	}
	
	private class OnBluetoothDeviceName implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnBluetoothDeviceName");
//			mFragmentOBD.setEditDialog(mEditDialogType);
			
			BluetoothCommunicator.getInstance().disconnect();
			return true;
		}
	}
	
	private class OnBluetoothPassword implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnBluetoothPassword");
//			mFragmentOBD.setEditDialog(mEditDialogType);
			BluetoothCommunicator.getInstance().disconnect();
			return true;
		}
	}
	
	private class OnSetVehicleInfo implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnSetVehicleInfo");
			
//			setDialog1ButtonContent(	getText(R.string.dialog_text_title_configuration), 
//										getText(R.string.dialog_text_body_configuration), 
//										getText(R.string.dialog_button_ok)	);
//			showDialog(VariousID.ALERT_DIALOG_1BUTTON_NORMAL);
			
			return true;
		}
	}
	
	private class OnMonitoringStop implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnMonitoringStop");
			
//			byte[] packetForBlockErase = mUpdateData.createNextPacketForBlockErase();
//			
//			try {
//				BluetoothCommunicator.getInstance().write(packetForBlockErase);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			return true;
		}
	}
	
	private class OnExternalBlockEraseComplete implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnExternalBlockEraseComplete");
//			byte[] packetForExternal = mUpdateData.createNextPacketForBlockErase();
//			
//			if(packetForExternal == null)
//			{
//				if(mUpdateData instanceof FirmwareData)
//				{
//					mExternalWriteQ.add(ExternalWriteType.FW_FILE_SIZE);
//					packetForExternal = ((FirmwareData)mUpdateData).createPacketForFileSize();
//				}
//				else if(mUpdateData instanceof VDBData)
//				{
//					mExternalWriteQ.add(ExternalWriteType.VDB_FILE_DATA);
//					packetForExternal = mUpdateData.createNextPacketForData();
//				}
//					
//			}
//			
//			try {
//				BluetoothCommunicator.getInstance().write(packetForExternal);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			return true;
		}
		
	}
	
	private class OnExternalWriteComplete implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnExternalWriteComplete");
			
//			ExternalWriteType type = mExternalWriteQ.get(0);
//			mExternalWriteQ.remove(0);
//			
//			byte[] sendPacket = null;
//			switch(type)
//			{
//			case FW_FILE_SIZE:
//				mDialogUpdateDevice.incrementProgressBy(mUpdateData.getCurrentProgress());
//				
//				sendPacket = mUpdateData.createNextPacketForData();
//				mExternalWriteQ.add(ExternalWriteType.FW_FILE_DATA);
//				break;
//			case FW_FILE_DATA:
//				mDialogUpdateDevice.incrementProgressBy(mUpdateData.getCurrentProgress());
//				sendPacket = mUpdateData.createNextPacketForData();
//				if(sendPacket == null)
//				{
//					mExternalWriteQ.add(ExternalWriteType.FW_CHECK_SUM);
//					sendPacket = ((FirmwareData)mUpdateData).createPacketForCheckSum();
//				}
//				else
//				{
//					mExternalWriteQ.add(ExternalWriteType.FW_FILE_DATA);
//				}
//				break;
//			case FW_CHECK_SUM:
//				sendPacket = mUpdateData.createPacketForReset();
//				mDialogUpdateDevice.dismiss();
//				removeDialog(VariousID.PROGRESS_UPDATE_DEVICE);
//				
//				try {
//					BluetoothCommunicator.getInstance().write(sendPacket);
//					BluetoothCommunicator.getInstance().disconnect();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//				return true;
//			case VDB_FILE_DATA:
//				mDialogUpdateDevice.incrementProgressBy(mUpdateData.getCurrentProgress());
//				sendPacket = mUpdateData.createNextPacketForData();
//				if(sendPacket == null)
//				{
//					sendPacket = mUpdateData.createPacketForReset();
//					mDialogUpdateDevice.dismiss();
//					removeDialog(VariousID.PROGRESS_UPDATE_DEVICE);
//					
//					try {
//						BluetoothCommunicator.getInstance().write(sendPacket);
//						BluetoothCommunicator.getInstance().disconnect();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					
//					return true;
//				}
//				else
//				{
//					mExternalWriteQ.add(ExternalWriteType.VDB_FILE_DATA);
//				} 
//				break;
//			}
//			
//			try {
//				BluetoothCommunicator.getInstance().write(sendPacket);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			return true;
		}
	}
	
	private class OnHWReset implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnHWReset");
			return true;
		}
	}
	
	private class OnBluetoothPinCode implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnBluetoothPinCode");
			//SharedPreferences pref = getSharedPreferences(PrefInfo.NAME, Activity.MODE_PRIVATE); 
			String password = "0000";//pref.getString(PrefInfo.CONFIG_BT_PASSWORD, "0000");
			
			
			PacketBuilder.getInstance().init((short)4);
			PacketBuilder.getInstance().putTar((byte)0xA0);
			PacketBuilder.getInstance().putID((byte)0x70);
			PacketBuilder.getInstance().putSubID((byte)0x50);
			byte[] bPassword = password.getBytes();
			PacketBuilder.getInstance().putDataBlock(bPassword);
			byte[] packetForPinCode = PacketBuilder.getInstance().buildOnBT();
			try {
				BluetoothCommunicator.getInstance().write(packetForPinCode);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	
	private byte[] createSupportPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x30);
		PacketBuilder.getInstance().putSubID((byte)0x10);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createSensorPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x20);
		PacketBuilder.getInstance().putSubID((byte)0x10);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createCurrentRepeatPacket(DrivingSupportWhole drivingSupportWhole)
	{
		if(!drivingSupportWhole.HasSupport)
			return null;
		
		PacketBuilder.getInstance().init((short)4);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x30);
		PacketBuilder.getInstance().putSubID((byte)0x40);
		byte[] supportData = drivingSupportWhole.buildSupportByte("0011", DrivingSupportWhole.INDEX_IGNITION_ON);
		PacketBuilder.getInstance().putDataBlock(supportData);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createSpeedBroadcastingPacket()
	{
		PacketBuilder.getInstance().init((short)4);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x20);
		PacketBuilder.getInstance().putSubID((byte)0x31);
		
		byte[] supportData = null;
		if((mProtocolNum == 0x000000A0) || (mProtocolNum == 0x000000A1))
			supportData = new byte[]{0x00, 0x0D, 0x00, 0x00 };
		else
			supportData = new byte[]{0x00, 0x10, 0x01, 0x00 };
		
		PacketBuilder.getInstance().putDataBlock(supportData);
	    
	    return PacketBuilder.getInstance().buildOnBT();
	}
	
	private class InvalidateWorker extends TimerTask
	{
		//private UpdateFragment mUpdateFragment;
		public InvalidateWorker()
		{
			//mUpdateFragment = new UpdateFragment();
		}
		@Override
		public void run() {
			//runOnUiThread(mUpdateFragment);
			System.out.println("Update fragment");
		}
	}
	
	
	private class OnGetProtocol implements IPacketProcessor
	{
		private GetProtocol mGetProtocol;
		public OnGetProtocol()
		{
			mGetProtocol = new GetProtocol();
		}
		
		
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mGetProtocol.parse(packet);
			mProtocolNum = mGetProtocol.ProtocolNumber;
			
			byte[] supportPacket = createSupportPacket();
			byte[] sensorPacket = createSensorPacket();
			try {
				BluetoothCommunicator.getInstance().write(supportPacket);
				BluetoothCommunicator.getInstance().write(sensorPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	public static final int DIVIDE_SIZE = 5;
	
	private class OnDrivingSupportWhole implements IPacketProcessor
	{
		private DrivingSupportWhole mDrivingSupportWhole;
		public OnDrivingSupportWhole()
		{
			mDrivingSupportWhole = new DrivingSupportWhole();
			mDrivingSupportWhole.init();
		}
		
		
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mDrivingSupportWhole.parse(packet);
			
			if(!mDrivingSupportWhole.HasSupport)
				return true;
			
			mDrivingBroadcastingCurrent.init(mDrivingSupportWhole.Group[DrivingSupportWhole.INDEX_CURRENT]);
			mDrivingBroadcastingAfterStart.init(mDrivingSupportWhole.Group[DrivingSupportWhole.INDEX_CURRENT]);
			
			byte[] currentRepeat = createCurrentRepeatPacket(mDrivingSupportWhole);
			byte[] speedBroadCasting = createSpeedBroadcastingPacket();
			try {
				BluetoothCommunicator.getInstance().write(currentRepeat);
				BluetoothCommunicator.getInstance().write(speedBroadCasting);
				
				mTimer = new Timer();
				mTimer.schedule(new InvalidateWorker() , 0, (1000 / DIVIDE_SIZE));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	
	private class OnSensorSupportWhole implements IPacketProcessor
	{
		private SensorSupportWhole mSensorSupportWhole;
		
		public OnSensorSupportWhole()
		{
			mSensorSupportWhole = new SensorSupportWhole();
		}
		
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mSensorSupportWhole.parse(packet);
			
			return true;
		}
	}
	
	private class OnDrivingBroadcastingCurrent implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mDrivingBroadcastingCurrent.parse(packet);
			mDrivingBroadcastingCurrent.currentRawData.Average = mDrivingBroadcastingAfterStart.AfterStartRawData.AverageFuelEff;
			mDrivingBroadcastingCurrent.currentRawData.FuelBills = mDrivingBroadcastingAfterStart.AfterStartRawData.FuelConsumptionForBills;
			mDrivingBroadcastingCurrent.currentRawData.Speed = mSensorBroadcating.Speed;
			
//			if( mDrivingBroadcastingCurrent.currentRawData.Coolant > LIMIT_COOLANT &&
//				(System.currentTimeMillis() - mCurrentTime) > (1000 * 60 * 3))
//    		{
//				if(mAlertDialog1ButtonNormal == null)
//				{
//					mAlertTitle = getText(R.string.dialog_text_title_trouble);
//					mAlertBody = getText(R.string.dialog_text_body_trouble);
//					showDialog(VariousID.ALERT_DIALOG_1BUTTON_NORMAL);
//					mCurrentTime = System.currentTimeMillis();
//				}
//				else
//				{
//					if(!mAlertDialog1ButtonNormal.isShowing())
//					{
//						mAlertTitle = getText(R.string.dialog_text_title_trouble);
//						mAlertBody = getText(R.string.dialog_text_body_trouble);
//						showDialog(VariousID.ALERT_DIALOG_1BUTTON_NORMAL);
//						mCurrentTime = System.currentTimeMillis();
//					}
//				}
//    		}
			
//			if(mEcoIndexFragment != null)
//			{
//				mEcoIndexFragment.setEcoRawData(mDrivingBroadcastingCurrent.currentRawData);
//			}
			System.out.println("broadcasting current, speed = "+mDrivingBroadcastingCurrent.currentRawData.Speed);
			return true;
		}
	}
	
	private class OnDrivingBroadcastingAfterStart implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mDrivingBroadcastingAfterStart.parse(packet);
//			Log.e(TAG, mDrivingBroadcastingAfterStart.AfterStartRawData.getLog());
			
//			if(mEcoInfoFragment != null)
//			{
//				mEcoInfoFragment.setEcoData(mDrivingBroadcastingAfterStart.AfterStartRawData);
//			}
			System.out.println("broadcasting afterstart received");
			
			return true;
		}
	}
	
	private class OnSensorBroadcating implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mSensorBroadcating.parse(packet);
			return true;
		}
	}
	
	private class OnSensorSingle implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			return true;
		}
	}
	
	private class OnDrivingSingle implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			return true;
		}
	}

}
