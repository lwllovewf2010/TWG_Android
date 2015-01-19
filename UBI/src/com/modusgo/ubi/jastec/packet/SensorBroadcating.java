package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class SensorBroadcating implements IPacket, IParser{
	public float Speed;
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] data = PacketParser.getInstance().getDataBlock();
		Speed =  Utils.toFloat(data, 1);		
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.SENSOR_BROADCASTING;
	}
}
