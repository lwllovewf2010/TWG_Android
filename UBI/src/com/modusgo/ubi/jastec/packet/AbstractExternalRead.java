package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public abstract class AbstractExternalRead implements IPacket, IParser{
	public enum MEMORY_SPEC{ DTC_COUNT, DTC_CONFIRM, DTC_PENDING, TRIP_ONLY };
	
	public abstract void parse(Byte[] packet);

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.EXTERNAL_READ_COMPLETE;
	}
}
