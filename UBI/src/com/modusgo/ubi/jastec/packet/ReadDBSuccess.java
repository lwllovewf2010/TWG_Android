package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class ReadDBSuccess implements IPacket, IParser{
	public String DBVersion;
	public boolean Invalid;
	public String Maker, Model0, Model1, Model2;
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] dataBlock = PacketParser.getInstance().getDataBlock();
		
		int currentIndex = 0;
		DBVersion = "";
		for(int i = 0;i < 30;i ++)
		{
			DBVersion += (char)dataBlock[currentIndex + i].byteValue();
		}
		
		currentIndex += 30;
		Invalid = false;
		if((dataBlock[30] != Protocol.STX) || (dataBlock[dataBlock.length - 1] != Protocol.ETX))
		{
			Invalid = true;
			return;
		}
		
		currentIndex += 1;
		Maker = "";
		for(int i = 0;i < 25; i++)
		{
			Maker += (char)dataBlock[currentIndex + i].byteValue();
		}
		
		currentIndex += 25;
		Model0 = "";
		for(int i = 0;i < 25; i++)
		{
			Model0 += (char)dataBlock[currentIndex + i].byteValue();
		}
		
		currentIndex += 25;
		Model1 = "";
		for(int i = 0;i < 25; i++)
		{
			Model1 += (char)dataBlock[currentIndex + i].byteValue();
		}
		
		currentIndex += 25;
		Model2 = "";
		for(int i = 0;i < 25; i++)
		{
			Model2 += (char)dataBlock[currentIndex + i].byteValue();
		}
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.READ_DB_SUCCESS;
	}

}
