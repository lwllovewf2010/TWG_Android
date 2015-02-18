package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.Calenda_RTC;
import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;
import com.modusgo.ubi.jastec.dtc.DTC01_04;
import com.modusgo.ubi.jastec.dtc.DTC02_60;
import com.modusgo.ubi.jastec.dtc.DTC03_20_61;
import com.modusgo.ubi.jastec.dtc.DTC06_2F;
import com.modusgo.ubi.jastec.dtc.DTC40;
import com.modusgo.ubi.jastec.dtc.DTCDataDb;
import com.modusgo.ubi.jastec.dtc.IDTCDescriptor;

public abstract class AbstractRealTimeReadDTC implements IPacket, IParser{
	public boolean HasDTC;
	public DTCDataDb DataDb;
	public IDTCDescriptor DTCDescriptor;

	public abstract PacketType getPacketType();
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] source = PacketParser.getInstance().getDataBlock();
				
		DataDb = DTCDataDb.createDTCDataDb();
		 
		// 0 : STX
		int dataIndex = 1;
		for(int i = 0;i < DataDb.SysID.SysName.length;i++)
		{
			DataDb.SysID.SysName[i] = (char)((byte)source[dataIndex + i]);
		}
		dataIndex += DataDb.SysID.SysName.length;
		
		for(int i=  0;i < DataDb.SysName.SysName.length;i++)
		{
			DataDb.SysName.SysName[i] = (char)((byte)source[dataIndex + i]);
		}
		dataIndex += DataDb.SysID.SysName.length;
		
		Byte[] byteRTC = new Byte[Calenda_RTC.SIZE];
		System.arraycopy(source, dataIndex, byteRTC, 0, Calenda_RTC.SIZE);
//		DataDb.RTC = Calenda_RTC.parse(byteRTC);
		DataDb.RTC.parse(byteRTC);
		dataIndex += Calenda_RTC.SIZE;
		
		DataDb.DTCStructToNum = source[dataIndex];
		dataIndex += 1;
		
		DataDb.DTCType = Utils.toShort(source, dataIndex);
		
		
		dataIndex += (Short.SIZE / Byte.SIZE);
		
		DataDb.DTCSize = source[dataIndex];
		dataIndex += 1;
		
		DataDb.FaultNum = source[dataIndex];
		dataIndex += 1;
		
		if(DataDb.FaultNum > 0 )
			HasDTC = true;
		
		System.arraycopy(source, dataIndex, DataDb.FaultCode, 0, DataDb.FaultCode.length);
		
		DTCDescriptor = null; 
		
		switch((DataDb.DTCType & 0x0FFF))
		{
		case 0x0001:
		case 0x0004:
			DTCDescriptor = new DTC01_04(DataDb);
			break;
		case 0x0002:
		case 0x0060:
			DTCDescriptor = new DTC02_60(DataDb);
			break;
		case 0x0003:
		case 0x0020:
		case 0x0061:
			DTCDescriptor = new DTC03_20_61(DataDb);
			break;
		case 0x0006:
		case 0x002F:
			DTCDescriptor = new DTC06_2F(DataDb);
			break;
		case 0x0040:
			DTCDescriptor = new DTC40(DataDb);
			break;
		}
	}
	
	

}
