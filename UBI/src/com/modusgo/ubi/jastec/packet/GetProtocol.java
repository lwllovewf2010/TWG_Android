package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class GetProtocol implements IPacket, IParser{
	public int ProtocolNumber;
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		Byte[] dataBlock = PacketParser.getInstance().getDataBlock();
		ProtocolNumber = Utils.toInt(dataBlock, 0);
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.GET_PROTOCOL;
	}

}
