package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class FirmwareVersion implements IPacket, IParser{

	public String FirmwareVersion;
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] dataBlock = PacketParser.getInstance().getDataBlock();
		
		FirmwareVersion = "";
		for(Byte data : dataBlock)
		{
			FirmwareVersion += (char)data.byteValue();
		}
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.FIRMWARE_VERSION;
	}

}
