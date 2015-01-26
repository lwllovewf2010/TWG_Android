package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;

public class RealTimeReadDTCConfirm extends AbstractRealTimeReadDTC{

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.REALTIME_READ_DTC_CONFIRM;
	}
}
