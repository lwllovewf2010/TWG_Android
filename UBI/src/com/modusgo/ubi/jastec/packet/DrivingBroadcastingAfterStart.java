package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.Calenda_RTC;
import com.modusgo.ubi.jastec.DriveRecodePerTrip;
import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class DrivingBroadcastingAfterStart implements IPacket, IParser{
	public boolean HasData;
	public AfterStartRawData AfterStartRawData;
	private boolean[] mGroupData; 
	
	public void init(boolean[] groupData)
	{
		HasData = false;
		if(mGroupData == null)
			mGroupData = new boolean[groupData.length];
		
		for(int i = 0;i < mGroupData.length; i++)
			mGroupData[i] = groupData[i];
		
		if(AfterStartRawData == null)
			AfterStartRawData = new AfterStartRawData();
			
		AfterStartRawData.init();
	}
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] data = PacketParser.getInstance().getDataBlock();
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
										
				AfterStartRawData.HardAccel = Utils.toShort(parsingData, 0);
				AfterStartRawData.HardDecel = Utils.toShort(parsingData, 2);
				break;
			case 1:		// unchecked..
				rawDataIndex += DriveRecodePerTrip.AverageStruct.SIZE;
				break;
			case 2:
				rawDataIndex += (Float.SIZE / Byte.SIZE);
				break;
			case 3:
				parsingData = new Byte[(Short.SIZE / Byte.SIZE)];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									(Short.SIZE / Byte.SIZE) );
				rawDataIndex += (Short.SIZE / Byte.SIZE);
				AfterStartRawData.MaximumSpeed = Utils.toShort(parsingData, 0);
				break;
			case 4:
				parsingData = new Byte[DriveRecodePerTrip.AverageSpeed.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.AverageSpeed.SIZE );
				rawDataIndex += DriveRecodePerTrip.AverageSpeed.SIZE;
				
				long average_speed = Utils.toLong4Byte(parsingData, 0);
				long totalDistance = Utils.toLong4Byte(parsingData, 4);
				long totalTime = Utils.toLong4Byte(parsingData, 8);
				
				AfterStartRawData.AverageSpeed = (int)(average_speed / 1000);
				AfterStartRawData.DrivingDistance = (int)(totalDistance / (1000 * 1000));
				AfterStartRawData.DrivingTime = Utils.convertMSToTime(totalTime);
				break;
			case 5:
				rawDataIndex += DriveRecodePerTrip.SpeedDivid.SIZE;
				break;
			case 6:
				parsingData = new Byte[DriveRecodePerTrip.DriveTime.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.DriveTime.SIZE );
				rawDataIndex += DriveRecodePerTrip.DriveTime.SIZE;
				
				long idleTime = Utils.toLong4Byte(parsingData, 0);
				long warmUpTime = Utils.toLong4Byte(parsingData, 28);
				
				AfterStartRawData.IdleTime = Utils.convertMSToTime(idleTime);
				AfterStartRawData.WarmingUpTime = Utils.convertMSToTime(warmUpTime);
				break;
			case 7:
				parsingData = new Byte[DriveRecodePerTrip.FuelConsum.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.FuelConsum.SIZE );
				rawDataIndex += DriveRecodePerTrip.FuelConsum.SIZE;
				
				long fuelConsumMass = Utils.toLong4Byte(parsingData, 0);
				float fuelEfficient = Utils.toFloat(parsingData, 4);
				
				AfterStartRawData.FuelConsumption = (int)(fuelConsumMass / 1000);
				AfterStartRawData.FuelEfficiency = (int)fuelEfficient;
				AfterStartRawData.AverageFuelEff = fuelEfficient;
				AfterStartRawData.FuelConsumptionForBills = (float)(fuelConsumMass / 1000f);
				break;
			case 8:
				parsingData = new Byte[DriveRecodePerTrip.Co2Struct.SIZE];
				System.arraycopy(	data, 
									rawDataIndex, 
									parsingData, 0, 
									DriveRecodePerTrip.Co2Struct.SIZE );
				rawDataIndex += DriveRecodePerTrip.Co2Struct.SIZE;
				
				long co2 = Utils.toLong4Byte(parsingData, 4);
				AfterStartRawData.CO2 = (int)(co2 / 1000);
				break;					
			}
		}
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.DRIVING_BROADCASTING_AFTER_START;
	}
	
	public static class AfterStartRawData implements IPacket 
	{
		public int HardAccel = 0;
		public int HardDecel = 0;
		public int MaximumSpeed = 0;
		public int AverageSpeed = 0;
		public int DrivingDistance = 0;
		public String DrivingTime = "00:00:00";
		public String WarmingUpTime = "00:00:00";
		public String IdleTime = "00:00:00";
		public int FuelConsumption = 0;
		public float FuelEfficiency = 0.0f;
		public float AverageFuelEff = 0.0f;
		public float FuelConsumptionForBills = 0.0f;
		public int CO2 = 0;
		
		public void init()
		{
			HardAccel = 0;
			HardDecel = 0;
			MaximumSpeed = 0;
			AverageSpeed = 0;
			DrivingDistance = 0;
			DrivingTime = "00:00:00";
			WarmingUpTime = "00:00:00";
			IdleTime = "00:00:00";
			FuelConsumption = 0;
			FuelEfficiency = 0.0f;
			AverageFuelEff = 0.0f;
			FuelConsumptionForBills = 0.0f;
			CO2 = 0;
		}
		
		@Override
		public PacketType getPacketType() {
			return Protocol.PacketType.DRIVING_BROADCASTING_AFTER_START;
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			AfterStartRawData afterStartRawData = new AfterStartRawData();
			afterStartRawData.HardAccel = HardAccel;
			afterStartRawData.HardDecel = HardDecel;
			afterStartRawData.MaximumSpeed = MaximumSpeed;
			afterStartRawData.AverageSpeed = AverageSpeed;
			afterStartRawData.DrivingDistance = DrivingDistance;
			afterStartRawData.DrivingTime = DrivingTime;
			afterStartRawData.WarmingUpTime = WarmingUpTime;
			afterStartRawData.FuelConsumption = FuelConsumption;
			afterStartRawData.FuelEfficiency = FuelEfficiency;
			afterStartRawData.AverageFuelEff = AverageFuelEff;
			afterStartRawData.FuelConsumptionForBills = FuelConsumptionForBills;
			afterStartRawData.CO2 = CO2;
			
			return afterStartRawData;
		}
	}
}
