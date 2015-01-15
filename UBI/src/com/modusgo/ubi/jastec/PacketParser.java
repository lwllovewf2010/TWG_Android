package com.modusgo.ubi.jastec;


public class PacketParser {
	// stx(1), tar(1), src(1), ID(1), subID(1), length(2), data(length), cs(1), cr(1)
	private Byte[] mPacket;
	
	private static PacketParser mInstance = new PacketParser();	
	private PacketParser(){	}
	
	public static PacketParser getInstance()
	{	
		return mInstance;
	}
	
	public void init(Byte[] packet)
	{
		mPacket = decode(packet);
	}
	
	public byte getTar()
	{
		return mPacket[1];
	}
	
	public byte getSrc()
	{
		return mPacket[2];
	}
	
	public byte getID()
	{
		return mPacket[3];
	}
	
	public byte getSubID()
	{
		return mPacket[4];
	}
	
	public short getLength()
	{
		// index : 5
		Byte[] length = new Byte[2];
		System.arraycopy(mPacket, 5, length, 0, 2);
		
		short sLength = (short)((length[0] << 8) | (length[1] & 0x00FF));	
		return sLength;
	}
	
	public Byte[] getDataBlock()
	{
		// index : 7
		short sLength = getLength();
		
		Byte[] dataBlock = new Byte[sLength];
		System.arraycopy(mPacket, 7, dataBlock, 0, sLength);
		
		return dataBlock;
	}
	
	public byte getCS()
	{
		return mPacket[mPacket.length - 2];
	}
	
	public Protocol.PacketType getPacketType()
	{
		byte src = getSrc();
		byte id = getID();
		byte subId = getSubID();
		
		int temp = (src << (8 * 2)) | (id << (8 * 1)) | subId;		
		int vonEcoId = (0x00FFFFFF) & temp;
		
		return Protocol.PacketType.fromValue(vonEcoId);
	}
	
	public String getLog() {
		String temp = "";
		for(int i = 0;i < mPacket.length; i++)
		{
			temp += String.format("%02X", mPacket[i]);
			temp += " ";
		}
		
		return temp;
	}

	private Byte[] decode(Byte[] packet)
	{
		int decodingCount = getDecodingCount(packet);
		if(decodingCount == 0)
			return  packet;
		
		Byte[] decodingPacket = new Byte[packet.length - decodingCount];
		
		for(int i = 0, currentPosition = 0;currentPosition < packet.length; i++, currentPosition++)
		{
			
			if(decodeRequired(packet[currentPosition]))
			{			        
				currentPosition += 1; 
				decodingPacket[i] = (byte)(packet[currentPosition] ^ Protocol.ECV);
				continue;
			}
			
			decodingPacket[i] = packet[currentPosition];			
		}
		
		return decodingPacket;
	}
	
	private int getDecodingCount(Byte[] packet)
	{
		int count = 0;
		for(int i = 0;i < packet.length; i++)
		{
			if(decodeRequired(packet[i]))
				count++;
		}
		
		return count;
	}
	
	private boolean decodeRequired(byte data)
	{
        return (data == Protocol.ESCAPE);
	}
}
