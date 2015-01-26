package com.modusgo.ubi.jastec.packet;

import android.os.Parcel;
import android.os.Parcelable;

import com.modusgo.ubi.jastec.Calenda_RTC;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Utils;
import com.modusgo.ubi.jastec.dtc.DTC01_04;
import com.modusgo.ubi.jastec.dtc.DTC02_60;
import com.modusgo.ubi.jastec.dtc.DTC03_20_61;
import com.modusgo.ubi.jastec.dtc.DTC06_2F;
import com.modusgo.ubi.jastec.dtc.DTC40;
import com.modusgo.ubi.jastec.dtc.DTCDataDb;
import com.modusgo.ubi.jastec.dtc.IDTCDescriptor;


public class DTCRead extends AbstractExternalRead{
	public boolean HasDTC;
	public IDTCDescriptor DTCDescriptor;
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] source = PacketParser.getInstance().getDataBlock();
				
		DTCDataDb dtc = DTCDataDb.createDTCDataDb();
		
		 
		// 0 : STX
		int dataIndex = 1;
		for(int i = 0;i < dtc.SysID.SysName.length;i++)
		{
			dtc.SysID.SysName[i] = (char)((byte)source[dataIndex + i]);
		}
		dataIndex += dtc.SysID.SysName.length;
		
		for(int i=  0;i < dtc.SysName.SysName.length;i++)
		{
			dtc.SysName.SysName[i] = (char)((byte)source[dataIndex + i]);
		}
		dataIndex += dtc.SysID.SysName.length;
		
		Byte[] byteRTC = new Byte[Calenda_RTC.SIZE];
		System.arraycopy(source, dataIndex, byteRTC, 0, Calenda_RTC.SIZE);
//		dtc.RTC = Calenda_RTC.parse(byteRTC);
		dtc.RTC.parse(byteRTC);
		dataIndex += Calenda_RTC.SIZE;
		
		dtc.DTCStructToNum = source[dataIndex];
		dataIndex += 1;
		
		dtc.DTCType = Utils.toShort(source, dataIndex);
		
		
		dataIndex += (Short.SIZE / Byte.SIZE);
		
		dtc.DTCSize = source[dataIndex];
		dataIndex += 1;
		
		dtc.FaultNum = source[dataIndex];
		dataIndex += 1;
		
		if(dtc.FaultNum > 0 )
			HasDTC = true;
		
		System.arraycopy(source, dataIndex, dtc.FaultCode, 0, dtc.FaultCode.length);
		
		DTCDescriptor = null; 
		
		switch((dtc.DTCType & 0x0FFF))
		{
		case 0x0001:
		case 0x0004:
			DTCDescriptor = new DTC01_04(dtc);
			break;
		case 0x0002:
		case 0x0060:
			DTCDescriptor = new DTC02_60(dtc);
			break;
		case 0x0003:
		case 0x0020:
		case 0x0061:
			DTCDescriptor = new DTC03_20_61(dtc);
			break;
		case 0x0006:
		case 0x002F:
			DTCDescriptor = new DTC06_2F(dtc);
			break;
		case 0x0040:
			DTCDescriptor = new DTC40(dtc);
			break;
		}
	}
	
	// ============================ Inner Class ============================
	public static class DTCItem implements Parcelable
	{
		private String mmCode;
		private String mmDateTime;
		private String mmOtherDateTimeFormat;
		
		public DTCItem(String code, String dateTime, String otherDateTime)
		{
			mmCode = code;
			mmDateTime = dateTime;
			mmOtherDateTimeFormat = otherDateTime;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel parcel, int i) {
			parcel.writeString(mmCode);
			parcel.writeString(mmDateTime);
			parcel.writeString(mmOtherDateTimeFormat);
		}
		
		public static final Creator<DTCItem> CREATOR = new Creator<DTCItem>() 
		{
			@Override
			public DTCItem[] newArray(int i) {
				return new DTCItem[i];
			}
			
			@Override
			public DTCItem createFromParcel(Parcel parcel) {
				DTCItem item = new DTCItem(parcel.readString(), parcel.readString(), parcel.readString());
				
				return item;
			}
		};
		
		public String getCode(){	return mmCode;	}
		public String getDateTime(){	return mmDateTime;	}
		public String getOtherDateTimeFormats(){	return mmOtherDateTimeFormat;	}
	}
	// ===============================================================
}
