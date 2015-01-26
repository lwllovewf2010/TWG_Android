package com.modusgo.ubi.jastec;


public class Protocol {
	public static final byte STX = 0X02;
	public static final byte ETX = 0X03;
	public static final byte CR = 0X0D;
	public static final byte SRC_BT = 0x12;
	
	public static final byte ESCAPE = 0x7D;
	public static final byte ECV = 0x20;
	
	public static final short TRIP_SIZE = 0X00C8;
	public static final int SECTOR_SIZE = 0x00010000;
	public static final int TRIP_ONLY_SECTOR1_START = 0X00360000;
//    public static final int TRIP_ONLY_SECTOR1_END = 0X0036FFFF;
//    public static final int TRIP_ONLY_SECTOR2_START = 0X00370000;
//    public static final int TRIP_ONLY_SECTOR2_END = 0X0037FFFF;
//    public static final int TRIP_ONLY_SECTOR3_START = 0X00380000;
//    
	
	
	public enum PacketType
	{
		GET_PROTOCOL(0X00900020),
		DRIVING_SUPPORT_WHOLE(0x00903010), 
		SENSOR_SUPPORT_WHOLE(0X00902010),
		SENSOR_BROADCASTING(0x00902031),
		DRIVING_BROADCASTING_CURRENT(0X00903041),
		DRIVING_BROADCASTING_AFTER_START(0X00903042),
		DRIVING_SINGLE(0X00903030),
		EXTERNAL_READ_COMPLETE(0X00811014),
		REALTIME_READ_DTC_CONFIRM(0x00907000),
		REALTIME_READ_DTC_PENDING(0x00907001),
		REMOVE_DTC(0X00905010),
		SENSOR_SELECT(0X00902021),
		SENSOR_SINGLE(0X00902030),
		DRIVING_LOG_ADDRESS(0x00900040),
		READ_DB_SUCCESS(0X00904000),
		READ_DB_ERROR(0X0090407F),
		FIRMWARE_VERSION(0x00A01010),
		RTC_READ(0X00A02010),
		RTC_WRITE(0X00A02020),
		READ_SERIAL_NUMBER_SUCCESS(0X00A05010),
		READ_SERIAL_NUMBER_INVALID(0X00A05040),
		BLUETOOTH_DEVICE_NAME(0X00A07040),
		BLUETOOTH_PASSWORD(0X00A07020),
		BLUETOOTH_PINCODE(0X00A07050),
		SET_VEHICLE_INFO(0X00900030),
		MONITORING_STOP(0x00901000),
		EXTERNAL_BLOCK_ERASE_COMPLETE(0X00814014),
		EXTERNAL_WRITE_COMPLETE(0X00812014),
		HW_RESET(0x00A0F000),
		
		READ_VIN(0x00904010),
		READ_MEID(0x00A05010);
		
		private int mValue;
		private PacketType(int value)
		{
			mValue = value;
		}
		
		public static PacketType fromOrdinal(int ordinal)
		{
			for(PacketType target : PacketType.values())
			{
				if(target.ordinal() == ordinal)
					return target;
			}
			
			return null;
		}
		
		public static PacketType fromValue(int value)
		{
			for(PacketType target : PacketType.values())
			{
				if(target.value() == value)
					return target;
			}
			return null;
		}
		
		public int value()
		{
			return mValue;
		}
	}
}
