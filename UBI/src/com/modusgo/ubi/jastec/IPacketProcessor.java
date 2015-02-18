package com.modusgo.ubi.jastec;

public interface IPacketProcessor {
	boolean onTransFunc(Byte[] packet);
}
