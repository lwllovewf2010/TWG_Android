package com.modusgo.ubi.jastec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.modusgo.ubi.Constants;
import com.modusgo.ubi.jastec.BluetoothCommunicator.OnConnectionListener;
import com.modusgo.ubi.jastec.BluetoothCommunicator.OnDataListener;
import com.modusgo.ubi.jastec.packet.AbstractRealTimeReadDTC;
import com.modusgo.ubi.jastec.packet.DTCRead.DTCItem;
import com.modusgo.ubi.jastec.packet.DrivingBroadcastingAfterStart;
import com.modusgo.ubi.jastec.packet.DrivingBroadcastingCurrent;
import com.modusgo.ubi.jastec.packet.DrivingSupportWhole;
import com.modusgo.ubi.jastec.packet.GetProtocol;
import com.modusgo.ubi.jastec.packet.ReadVIN;
import com.modusgo.ubi.jastec.packet.RealTimeReadDTCConfirm;
import com.modusgo.ubi.jastec.packet.SensorBroadcating;

public class JastecManager implements OnConnectionListener, OnDataListener{

	private final static String TAG = "Jastec";
	
	private static JastecManager sInstance;

    private BluetoothAdapter mBtAdapter;
	protected Map<Protocol.PacketType, IPacketProcessor> mMapProcessor;

	private DrivingBroadcastingCurrent mDrivingBroadcastingCurrent;
	private DrivingBroadcastingAfterStart mDrivingBroadcastingAfterStart;
	private SensorBroadcating mSensorBroadcating;
	
	private OnSensorListener mOnSensorListener;
	
	SharedPreferences prefs;
	
	private int mProtocolNum;
	
    private String mDeviceName;
    private String mDeviceAddress;

	private OnConnectionListener mOnConnectionListener;
	private boolean mConnectionStarted = false;
	
	public static JastecManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new JastecManager(context);
		}
		return sInstance;
	}
	
	private JastecManager(Context context) {

        mMapProcessor = new HashMap<Protocol.PacketType, IPacketProcessor>();
        
        mMapProcessor.put(Protocol.PacketType.BLUETOOTH_DEVICE_NAME,	 			new OnBluetoothDeviceName());
		mMapProcessor.put(Protocol.PacketType.BLUETOOTH_PASSWORD,		 			new OnBluetoothPassword());
		mMapProcessor.put(Protocol.PacketType.MONITORING_STOP,			 			new OnMonitoringStop());
		mMapProcessor.put(Protocol.PacketType.BLUETOOTH_PINCODE, 					new OnBluetoothPinCode());
		
		mMapProcessor.put(Protocol.PacketType.GET_PROTOCOL, 					new OnGetProtocol());
		mMapProcessor.put(Protocol.PacketType.DRIVING_SUPPORT_WHOLE, 			new OnDrivingSupportWhole());
		mMapProcessor.put(Protocol.PacketType.SENSOR_BROADCASTING, 				new OnSensorBroadcating());

		mMapProcessor.put(Protocol.PacketType.READ_VIN, 					new OnReadVIN());
		mMapProcessor.put(Protocol.PacketType.REALTIME_READ_DTC_CONFIRM, 	new OnRealTimeReadDTCConfirm());
		

        mDrivingBroadcastingCurrent = new DrivingBroadcastingCurrent();
		mDrivingBroadcastingAfterStart = new DrivingBroadcastingAfterStart();
		mSensorBroadcating = new SensorBroadcating();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mDeviceAddress = prefs.getString(Constants.PREF_JASTEC_ADDRESS, "");
		mDeviceName = prefs.getString(Constants.PREF_JASTEC_NAME, "");
		
	}
	
	public float getLastRPM(){
		if(mSensorBroadcating == null)
			return 0;
		else
			return mSensorBroadcating.RPM;
	}
	
	public float getLastSpeed(){
		if(mSensorBroadcating == null)
			return 0;
		else
			return mSensorBroadcating.Speed;
	}
	
	public void setOnConnectionListener(OnConnectionListener listener){
		mOnConnectionListener = listener;		
	}
	
	public void setOnSensorListener(OnSensorListener listener){
		mOnSensorListener = listener;
	}
	
	public void connect(String deviceAddress, String deviceName){
		mDeviceAddress = deviceAddress;
		mDeviceName = deviceName;
		BluetoothCommunicator btCommunicator = BluetoothCommunicator.getInstance();
		
		if(!mConnectionStarted){
			if(btCommunicator.isConnected()){
				btCommunicator.disconnect();
			}
			
			btCommunicator.setOnConnectionListener(this);
			btCommunicator.setOnDataListener(this);
			
			BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(mDeviceAddress);
			btCommunicator.connect(bluetoothDevice);
			
			mConnectionStarted = true;
		}
	}
	
	public void connect(){
		BluetoothCommunicator btCommunicator = BluetoothCommunicator.getInstance();
		
		if(!mConnectionStarted){
			if(!TextUtils.isEmpty(mDeviceAddress)){
				if(!btCommunicator.isConnected()){
					btCommunicator.setOnConnectionListener(this);
					btCommunicator.setOnDataListener(this);
				
					BluetoothDevice bluetoothDevice = mBtAdapter.getRemoteDevice(mDeviceAddress);
					btCommunicator.connect(bluetoothDevice);
	
					mConnectionStarted = true;
				}
				else{
					byte[] getProtocolPacket = createProtocolPacket();
					try {
						BluetoothCommunicator.getInstance().write(getProtocolPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void disconnect(){
		try{
			BluetoothCommunicator.getInstance().disconnect();
		}
		catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisconnected(Exception e) {
		System.out.println("Bt disconnected");
		mConnectionStarted = false;
		if(mOnConnectionListener!=null)
			mOnConnectionListener.onDisconnected(e);
		
	}
	
	@Override
	public void onConnected() {
		System.out.println("Bt connected");
		mConnectionStarted = false;
		
		String addressStored = prefs.getString(Constants.PREF_JASTEC_ADDRESS, "");
		
		if(!addressStored.equals(mDeviceAddress))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(Constants.PREF_JASTEC_ADDRESS, mDeviceAddress);
			editor.putString(Constants.PREF_JASTEC_NAME, mDeviceName);
			editor.commit();
		}
		
		if(mOnConnectionListener!=null)
			mOnConnectionListener.onConnected();
		
		byte[] getProtocolPacket = createProtocolPacket();
		try {
			BluetoothCommunicator.getInstance().write(getProtocolPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onReceived(Byte[][] packets)
	{
		System.out.println("packet received");
		for(int i = 0;i < packets.length; i++)
		{
			PacketParser.getInstance().init(packets[i]);
			Protocol.PacketType type = PacketParser.getInstance().getPacketType();

			System.out.println("packet type = " + type);
			//Toast.makeText(this, "packet received type = "+type+", count = "+packets.length, Toast.LENGTH_SHORT).show();
//			Log.e(TAG, "type : " + type);
//			Log.e(TAG, PacketParser.getInstance().getLog());
			
			IPacketProcessor packetProcessor = mMapProcessor.get(type);
			if(packetProcessor == null)
			{
//				Toast.makeText(getApplicationContext(), "do not exist the type("+type+")", Toast.LENGTH_LONG).show();
				continue;
			}
			
			if(!packetProcessor.onTransFunc(packets[i]))
			{
//				Toast.makeText(getApplicationContext(), "do not proceed the type("+type+")", Toast.LENGTH_LONG).show();
				continue;
			}
		}
	}
	
	public interface OnSensorListener{
		
		void onTripStart();
		void onTripStop();
		void onPing();
	}
	
	private byte[] createProtocolPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x00);
		PacketBuilder.getInstance().putSubID((byte)0x20);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	private byte[] createVINPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x40);
		PacketBuilder.getInstance().putSubID((byte)0x10);
		
		return PacketBuilder.getInstance().buildOnBT();
	}
	
	public byte[] createRealTimeDTCPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x70);
		PacketBuilder.getInstance().putSubID((byte)0x00);
		
		byte[] packet = PacketBuilder.getInstance().buildOnBT();
		
	    return packet;
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
	
	private byte[] createSupportPacket()
	{
		PacketBuilder.getInstance().init((short)0);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x30);
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
	
	private static String protocolType = "";
	
	private byte[] createSpeedAndRPMBroadcastingPacket()
	{
		PacketBuilder.getInstance().init((short)12);
		PacketBuilder.getInstance().putTar((byte)0x90);
		PacketBuilder.getInstance().putID((byte)0x20);
		PacketBuilder.getInstance().putSubID((byte)0x31);
		
		byte[] supportData = null;
		if((mProtocolNum == 0x000000A0) || (mProtocolNum == 0x000000A1)){
			supportData = new byte[]{0x00, 0x0D, 0x00, 0x00,
									 0x00, 0x0C, 0x00, 0x00,
									 0x00, 0x42, 0x00, 0x00};
			protocolType = "generic";
			
		}
		else{
			supportData = new byte[]{0x00, 0x10, 0x01, 0x00,
									 0x00, 0x13, 0x01, 0x00,
									 0x00, 0x16, 0x01, 0x00};
			protocolType = "enhanced";
		}
		
		PacketBuilder.getInstance().putDataBlock(supportData);
	    
	    return PacketBuilder.getInstance().buildOnBT();
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
			byte[] getProtocolPacket = createProtocolPacket();
			byte[] vinPacket = createVINPacket();
			try {				
				BluetoothCommunicator.getInstance().write(packetForPinCode);
				BluetoothCommunicator.getInstance().write(getProtocolPacket);
				BluetoothCommunicator.getInstance().write(vinPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
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
			
			System.out.println("Protocol received: "+mProtocolNum);
			
			byte[] supportPacket = createSupportPacket();
//			byte[] sensorPacket = createSensorPacket();
//			byte[] speedBroadCasting = createSpeedAndRPMBroadcastingPacket();
			try {
				BluetoothCommunicator.getInstance().write(supportPacket);
//				BluetoothCommunicator.getInstance().write(sensorPacket);
//				BluetoothCommunicator.getInstance().write(speedBroadCasting);
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
			
			System.out.println("mDrivingSupportWhole, hasSupport = "+mDrivingSupportWhole.HasSupport);
			
			if(!mDrivingSupportWhole.HasSupport)
				return true;
			
			mDrivingBroadcastingCurrent.init(mDrivingSupportWhole.Group[DrivingSupportWhole.INDEX_CURRENT]);
			mDrivingBroadcastingAfterStart.init(mDrivingSupportWhole.Group[DrivingSupportWhole.INDEX_CURRENT]);
			
			byte[] currentRepeat = createCurrentRepeatPacket(mDrivingSupportWhole);
			byte[] speedBroadCasting = createSpeedAndRPMBroadcastingPacket();
			byte[] realTimeDTC = createRealTimeDTCPacket();
			try {
				BluetoothCommunicator.getInstance().write(currentRepeat);
				BluetoothCommunicator.getInstance().write(speedBroadCasting);
				BluetoothCommunicator.getInstance().write(realTimeDTC);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	static float lastPRM;
	static long firstZeroSpeedTimeMillis;
	
	private class OnSensorBroadcating implements IPacketProcessor
	{
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mSensorBroadcating.parse(packet);
			
			if(mOnSensorListener!=null)
				mOnSensorListener.onPing();
			
			System.out.println("Sensor braodcasting: ");
			System.out.println("protocol = " + protocolType);
			System.out.println("Speed = "+mSensorBroadcating.Speed);
			System.out.println("RPM = "+mSensorBroadcating.RPM);
			System.out.println("Voltage = "+mSensorBroadcating.voltage);
			
			if(mSensorBroadcating.Speed>0){
				firstZeroSpeedTimeMillis = 0;
				if(mOnSensorListener!=null){
					mOnSensorListener.onTripStart();
				}
			}
			
			if(mSensorBroadcating.Speed == 0 && lastPRM == mSensorBroadcating.RPM){
				
				if(firstZeroSpeedTimeMillis==0)
					firstZeroSpeedTimeMillis = System.currentTimeMillis();
				
				if(System.currentTimeMillis() - firstZeroSpeedTimeMillis >= 5000){
					if(mOnSensorListener!=null){
						mOnSensorListener.onTripStop();
					}
				}

				Bugsnag.addToTab("Jastec", "Protocal", protocolType);
				Bugsnag.addToTab("Jastec", "Speed", mSensorBroadcating.Speed);
				Bugsnag.addToTab("Jastec", "RPM", mSensorBroadcating.RPM);
				Bugsnag.addToTab("Jastec", "prev RPM", lastPRM);
				Bugsnag.addToTab("Jastec", "Voltage", mSensorBroadcating.voltage);
				Bugsnag.addToTab("Jastec", "VIN", prefs.getString(Constants.PREF_JASTEC_VEHICLE_VIN, "n/a"));
				Bugsnag.notify(new RuntimeException("Jastec sensor broadcasting, speed = 0 for " + (System.currentTimeMillis() - firstZeroSpeedTimeMillis) + "seconds, RPM: "+lastPRM));
			}
			
			lastPRM = mSensorBroadcating.RPM;
			
			return true;
		}
	}
	
	private class OnReadVIN implements IPacketProcessor
	{
		private ReadVIN mReadVIN;
		public OnReadVIN()
		{
			mReadVIN = new ReadVIN();
		}
		
		@Override
		public boolean onTransFunc(Byte[] packet) {
			mReadVIN.parse(packet);
			prefs.edit().putString(Constants.PREF_JASTEC_VEHICLE_VIN, mReadVIN.VIN).commit();
			return true;
		}
	}
	
	private class OnRealTimeReadDTCConfirm implements IPacketProcessor
	{
		private AbstractRealTimeReadDTC mRealTimeReadDTCConfirm;
		public OnRealTimeReadDTCConfirm()
		{
			mRealTimeReadDTCConfirm = new RealTimeReadDTCConfirm();
		}
		@Override
		public boolean onTransFunc(Byte[] packet) {
			Log.e(TAG, "OnRealTimeReadDTCConfirm");
			
			mRealTimeReadDTCConfirm.parse(packet);
			Log.e(TAG, "mRealTimeReadDTCConfirm.DataDb.DTCType : " + String.format("%02X", mRealTimeReadDTCConfirm.DataDb.DTCType));
			String dtcCodes = prefs.getString(Constants.PREF_JASTEC_DTCS, "");
			
			DTCItem[] dtcItmes = mRealTimeReadDTCConfirm.DTCDescriptor.makeDTCItem();
			for (int i = 0; i < dtcItmes.length; i++) {
				String code = dtcItmes[i].getCode();
				
				if(!dtcCodes.contains(""+code)){
					if(dtcCodes.isEmpty())
						dtcCodes += code;
					else
						dtcCodes += ", " + code;
				}
			}
			prefs.edit().putString(Constants.PREF_JASTEC_DTCS, dtcCodes).commit();
			
			System.out.println("DTCs : "+dtcCodes);
			
			if(mRealTimeReadDTCConfirm.DataDb.DTCType == 0x0020)
			{
				Log.e(TAG, "mRealTimeReadDTCConfirm.DataDb.DTCType : " + String.format("%02X", mRealTimeReadDTCConfirm.DataDb.DTCType));
				//mListConfirmation.add(mRealTimeReadDTCConfirm.DTCDescriptor);
			}
			return true;
		}
	}
}
