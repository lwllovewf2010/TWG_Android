package com.modusgo.ubi.jastec;


public class PacketBuilder {
	// stx(1), tar(1), src(1), ID(1), subID(1), length(2), data(length), cs(1), cr(1)
	private static final int SIZE = 1 + 1 + 1 + 1 + 1 + 2 + 1 + 1;
	private byte[] mPacket;
	
	private static PacketBuilder mInstance = new PacketBuilder();
	
	private PacketBuilder(){	}
	
	public static PacketBuilder getInstance()
	{
		return mInstance;
	}
	
	public void init(short length)
	{
		mPacket = new byte[SIZE + length];
		
		putLength(length);
	}
	
	public void putTar(byte tar)
	{
		mPacket[1] = tar;
	}
	
	public void putSrc(byte src)
	{
		mPacket[2] = src;
	}
	
	public void putID(byte ID)
	{
		mPacket[3] = ID;
	}
	
	public void putSubID(byte subID)
	{
		mPacket[4] = subID;
	}
	
	public void putLength(short length)
	{
		// index : 5
//		mPacket[5] = (byte)(length & 0x00FF); 
//		mPacket[6] = (byte)(length >> 8);
		
		mPacket[5] = (byte)(length >> 8); 
		mPacket[6] = (byte)(length & 0x00FF);
	}
	
	public void putDataBlock(byte[] packet)
	{
		System.arraycopy(packet, 0, mPacket, 7, packet.length);
	}
	
	public byte[] build()
	{
		mPacket[0] = Protocol.STX;
		byte cs = 0x00;
		for(int i = 0;i < mPacket.length - 2; i++)
		{
			
			cs += mPacket[i];
		}
		
		mPacket[mPacket.length - 2] = cs;
		mPacket[mPacket.length - 1] = Protocol.CR;		
		
		return encode(mPacket);
	}
	
	public byte[] buildOnBT()
	{
		mPacket[2] = Protocol.SRC_BT;
		return build();
	}
	
	private byte[] encode(byte[] packet)
	{
		int encodingCount = getEncodingCount(packet);
		if(encodingCount == 0)
			return  packet;
		
		byte[] encodingPacket = new byte[packet.length + encodingCount];
		
		for(int i = 0, currentPosition = 0;i < packet.length; i++, currentPosition++)
		{
			if(i == 0 || i == packet.length - 1)
			{
				encodingPacket[currentPosition] = packet[i];
				continue;
			}
			
			if(encodeRequired(packet[i]))
			{
				encodingPacket[currentPosition] = Protocol.ESCAPE;
				currentPosition += 1;
				encodingPacket[currentPosition] = (byte)(packet[i] ^ Protocol.ECV);
				continue;
			}
			
			encodingPacket[currentPosition] = packet[i];			
		}
		
		return encodingPacket;
	}
	
	private int getEncodingCount(byte[] packet)
	{
		int count = 0;
		for(int i = 0;i < packet.length; i++)
		{
			if(i == 0 || i == packet.length - 1)
				continue;
			
			if(encodeRequired(packet[i]))
				count++;
		}
		
		return count;
	}
	
	private boolean encodeRequired(byte data)
	{
		if((data == Protocol.STX) || (data == Protocol.CR) || (data == Protocol.ESCAPE))
            return true;

        return false;
	}
	
	
}
