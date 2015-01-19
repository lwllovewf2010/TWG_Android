package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class SensorSupportWhole implements IPacket, IParser{
	public int DataBlockLength;
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		
		DataBlockLength = PacketParser.getInstance().getLength();
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.SENSOR_SUPPORT_WHOLE;
	}
}
