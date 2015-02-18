package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class SensorBroadcating implements IPacket, IParser{
	public float Speed;
	public float RPM;
	public float voltage;
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] data = PacketParser.getInstance().getDataBlock();
		
		System.out.println("Sensor broadcasting packet");
		for (int j = 0; j < data.length; j++) {
			System.out.format("%02X ", data[j]);
		}
		System.out.println();
		
		Speed =  Utils.toFloat(data, 1);		
		System.out.println("Sensor broadcasting data length: "+data.length);
		RPM = Utils.toFloat(data, 6);
		voltage = Utils.toFloat(data, 11);
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.SENSOR_BROADCASTING;
	}
}
