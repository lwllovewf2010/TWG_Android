package com.modusgo.ubi.jastec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothCommunicator {
	private static final String TAG = "BluetoothCommunicator";
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	
	private Activity mActivity;
	private BluetoothWorker mBluetoothDataWorker;
	private boolean mIsConnected;
	
	private static OnDataListener mOnDataListener;
	private static OnConnectionListener mOnConnectionListener;
	private static final BluetoothCommunicator mInstnace = new BluetoothCommunicator();
	
	private BluetoothCommunicator(){	}
	public static final BluetoothCommunicator getInstance(){	return mInstnace;	}
	
	public void connect(BluetoothDevice bluetoothDevice)
	{
		mBluetoothDataWorker = new BluetoothWorker(bluetoothDevice);
		mBluetoothDataWorker.start();
	}
	
	public void disconnect()
	{
		mBluetoothDataWorker.disconnect();
	}
	
	public void write(byte[] packet) throws IOException
	{
		mBluetoothDataWorker.write(packet);
	}
	
	public void setActivity(Activity activity)
	{
		mActivity = activity;
	}
	
	public void setOnDataListener(OnDataListener onDataListener)
	{
		mOnDataListener = onDataListener;
	}
	
	public void setOnConnectionListener(OnConnectionListener onConnectionListener)
	{
		mOnConnectionListener = onConnectionListener;
	}
	
	public boolean isConnected()
	{
		return mIsConnected;
	}
	
	/*******************************************************************
	 * interface
	 *******************************************************************/
//	public static interface OnDataListener
//	{
//		void onReceived(byte[] packet);
//	}
	
	public static interface OnDataListener
	{
		void onReceived(Byte[][] packets);
	}
	
	public static interface OnConnectionListener
	{
		void onConnected();
		void onDisconnected(Exception e);
	}
	
	/*******************************************************************
	 * interface
	 *******************************************************************/
//	private class PacketTransferer implements Runnable
//	{
//		private byte[] mmData;
//		public PacketTransferer(byte[] data){
//			mmData = data;
//		}
//		
//		@Override
//		public void run() {
//			mOnDataListener.onReceived(mmData);
//		}
//	}
	
	private class PacketTransferer implements Runnable
	{
		private Byte[][] mmData;
		public PacketTransferer(Byte[][] data){
			mmData = data;
		}
		
		@Override
		public void run() {
			mOnDataListener.onReceived(mmData);
		}
	}
	
	private class ConnectionTransferer implements Runnable
	{
		private boolean mmIsConnected;
		private Exception mmException;
		public ConnectionTransferer(boolean isConnected, Exception e)
		{
			mIsConnected = isConnected;
			mmIsConnected = isConnected;
			mmException = e;
		}
		@Override
		public void run() {
			if(mmIsConnected)
				mOnConnectionListener.onConnected();
			else
				mOnConnectionListener.onDisconnected(mmException);
		}
	}
	
	/*******************************************************************
	 * thread
	 *******************************************************************/
	private class BluetoothWorker extends Thread
	{
		private boolean mmIsRunning;
		private BluetoothSocket mmBluetoothSocket;
		private BluetoothDevice mmBluetoothDevice;
		private InputStream mmInputStream;
		private OutputStream mmOutputStream;
		private RawDataBuilder mmJasatecPacketBuilder;
		
		public BluetoothWorker(BluetoothDevice bluetoothDevice)
		{
			mmBluetoothDevice = bluetoothDevice;
			mmIsRunning = true;
			mmJasatecPacketBuilder = new RawDataBuilder();
			mmJasatecPacketBuilder.init();
		}
		
		public void write(byte[] packet) throws IOException
		{
			mmOutputStream.write(packet);
			String temp = "";
			for(int i = 0;i < packet.length;i++)
			{
				temp += String.format("%02X", packet[i]);
				temp += " ";
			}
			
			Log.e(TAG, "write : " + temp);
		}
		
		public void disconnect()
		{
			try
			{
				setRunning(false);
				mmInputStream.close();
				mmOutputStream.close();
				mmBluetoothSocket.close();
//				synchronized (mActivity) {
//					mActivity.runOnUiThread(new ConnectionTransferer(false));
//				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		public void setRunning(boolean isRunning)
		{
			synchronized (this) 
			{
				mmIsRunning = isRunning;
			}
		}
		@Override
		public void run() {
			try
			{
				mmBluetoothSocket = mmBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
				mmBluetoothSocket.connect();
				mmInputStream = mmBluetoothSocket.getInputStream();
				mmOutputStream = mmBluetoothSocket.getOutputStream();
				synchronized (mActivity) {
					mActivity.runOnUiThread(new ConnectionTransferer(true, null));
				}
				
				byte[] buff = new byte[1024];
				byte[] receivedThisPacket;
				int size = 0;
				while(mmIsRunning)
				{
					size = mmInputStream.read(buff);
					if( size == 0)
					{
						synchronized (mActivity) {
							mActivity.runOnUiThread(new ConnectionTransferer(false, new Exception("data readed count is 0")));
						}
						continue;
					}
						
					receivedThisPacket = new byte[size];
					
					System.arraycopy(buff, 0, receivedThisPacket, 0, size);
					String log = "";
					for(int i = 0;i < receivedThisPacket.length; i++)
					{
						log += String.format(" %02X", receivedThisPacket[i]);
					}
					
					Log.e(TAG, log);
					
					mmJasatecPacketBuilder.add(receivedThisPacket);
					Byte[][] packets = mmJasatecPacketBuilder.build();
					synchronized (mActivity) {
						mActivity.runOnUiThread(new PacketTransferer(packets));
					}	
					
					Thread.sleep(5);
				}	 
			} catch(IOException e)
			{
				e.printStackTrace();
				synchronized (mActivity) {
					mActivity.runOnUiThread(new ConnectionTransferer(false, e));
				}
			} catch(InterruptedException e)
			{
				e.printStackTrace();
				synchronized (mActivity) {
					mActivity.runOnUiThread(new ConnectionTransferer(false, e));
				}
			}
			
		}
	}
}
