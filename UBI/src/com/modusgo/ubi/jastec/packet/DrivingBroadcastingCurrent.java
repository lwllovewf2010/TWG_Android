package com.modusgo.ubi.jastec.packet;

import android.util.Log;

import com.modusgo.ubi.jastec.Calenda_RTC;
import com.modusgo.ubi.jastec.DriveRecodePerTrip;
import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class DrivingBroadcastingCurrent implements IPacket, IParser{
	public boolean HasData;
	public CurrentRawData currentRawData;
	private boolean[] mGroupData; 
	
	public void init(boolean[] groupData)
	{
		HasData = false;
		if(mGroupData == null)
			mGroupData = new boolean[groupData.length];
		
		for(int i = 0;i < mGroupData.length; i++)
			mGroupData[i] = groupData[i];
		
		if(currentRawData == null)
			currentRawData = new CurrentRawData();
			
		currentRawData.init();
		
	}
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] data = PacketParser.getInstance().getDataBlock();
		Log.e("DrivingBroadcastingCurrent", "data.length : " + data.length);
		if((data == null) || (data.length == 0))
		{
			HasData = false;
			return;
		}
					
		int rawDataIndex = 0;
		// start
		rawDataIndex += Calenda_RTC.SIZE;
		// stop
		rawDataIndex += Calenda_RTC.SIZE;
		
		
		Byte[] parsingData;
		for(int i = 0;i < mGroupData.length;i++)
		{
			if(!mGroupData[i])
				continue;
			
			switch(i)
			{
			case 0:
				parsingData = new Byte[DriveRecodePerTrip.Accelation.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.Accelation.SIZE );
				rawDataIndex += DriveRecodePerTrip.Accelation.SIZE;
										
				currentRawData.HardAccel = Utils.toShort(parsingData, 0);
				currentRawData.HardDecel = Utils.toShort(parsingData, 2);
				break;
			case 1:
				rawDataIndex += DriveRecodePerTrip.AverageStruct.SIZE;
				break;
			case 2:
				// Engine Coolant Temperature
				parsingData = new Byte[Float.SIZE / Byte.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									(Float.SIZE / Byte.SIZE) );				

				currentRawData.Coolant = Utils.toFloat(parsingData, 0); 
				rawDataIndex += (Float.SIZE / Byte.SIZE);
				break;
			case 3:
				// Max Vehicle Speed
				rawDataIndex += (Short.SIZE / Byte.SIZE);
				break;
			case 4:
				rawDataIndex += DriveRecodePerTrip.AverageSpeed.SIZE;
				break;
			case 5:
				rawDataIndex += DriveRecodePerTrip.SpeedDivid.SIZE;
				break;
			case 6:
				rawDataIndex += DriveRecodePerTrip.DriveTime.SIZE;
				break;
			case 7:
				parsingData = new Byte[DriveRecodePerTrip.FuelConsum.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.FuelConsum.SIZE );
				
				currentRawData.Now = Utils.toFloat(parsingData, 4);
				rawDataIndex += DriveRecodePerTrip.FuelConsum.SIZE;				
				break;
			case 8:
				rawDataIndex += DriveRecodePerTrip.Co2Struct.SIZE;
				break;					
			}
		}
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.DRIVING_BROADCASTING_CURRENT;
	}
	
	public static class CurrentRawData implements IPacket
	{
		public float Average = 0.0f;
		public float Now = 0.0f;
		public float FuelBills = 0.0f;
		public int HardAccel = 0;
		public int HardDecel = 0;
		public float Coolant = 0.0f;
		public float Speed = 0.0f;
		
		public void init()
		{
			Average = 0.0f;
			Now = 0.0f;
			FuelBills = 0.0f;
			HardAccel = 0;
			HardDecel = 0;
			Coolant = 0.0f;
			Speed = 0.0f;
		}
		
		public String getLog()
		{
			String temp = "";
			temp += "Speed : " + String.format("%04d", (int)Speed) + "\n";
			temp += "Average : " + String.format("%5.2f", Average) + "\n";
			temp += ", Now : " + String.format("%5.2f", Now) + "\n";
			temp += ", FuelBills : " + String.format("%5.2f", FuelBills) + "\n";
			temp += ", HardAccel : " + String.format("%03d", HardAccel) + "\n";
			temp += ", HardDecel : " + String.format("%03d", HardDecel) + "\n";
			temp += ", Coolant : " + String.format("%5.2f", Coolant) + "\n";
			
			
			return temp;
		}
		@Override
		public PacketType getPacketType() {
			return Protocol.PacketType.DRIVING_BROADCASTING_CURRENT;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			CurrentRawData currentRawData = new CurrentRawData();			
			
			currentRawData.Average = Average;
			currentRawData.Now = Now;
			currentRawData.FuelBills = FuelBills;
			currentRawData.HardAccel = HardAccel;
			currentRawData.HardDecel = HardDecel;
			currentRawData.Coolant = Coolant;
			currentRawData.Speed = Speed;
			
			return currentRawData;
		}
	}
}
