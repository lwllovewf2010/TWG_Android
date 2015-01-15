package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class SerialNumberSuccess implements IPacket, IParser{
	public String SerialNumber;
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] dataBlock = PacketParser.getInstance().getDataBlock();
		SerialNumber = "";
		
		if(dataBlock.length == 0)
			return;
		
		for(int i = 0; i < 17; i++)
		{
			SerialNumber += (char)dataBlock[i].byteValue();
		}
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.READ_SERIAL_NUMBER_SUCCESS;
	}

}
