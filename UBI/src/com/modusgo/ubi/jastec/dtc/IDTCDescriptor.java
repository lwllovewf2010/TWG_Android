package com.modusgo.ubi.jastec.dtc;

import com.modusgo.ubi.jastec.packet.DTCRead.DTCItem;

	
public interface IDTCDescriptor
{
	public enum MakerType { HYUNDAI_KIA, DAEWOO, SAMSUNG, SSANGYONG };
	
	boolean isThisMakerType(MakerType makerType);
	DTCItem[] makeDTCItem();
	int DTCCodeCount();
	boolean hasPending();
	
}
