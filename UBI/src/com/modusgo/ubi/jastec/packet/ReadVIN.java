package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class ReadVIN implements IPacket, IParser{
	public String VIN;
	
	@Override
	public void parse(Byte[] packet) {
		VIN = "";
		PacketParser.getInstance().init(packet);
		Byte[] data = PacketParser.getInstance().getDataBlock();
		
		System.out.println("Read VIN packet");
		for(int i = 0;i < data.length;i ++)
		{
			VIN += (char)data[i].byteValue();
		}
		System.out.println("VIN: " + VIN);
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.READ_VIN;
	}
}
