package com.modusgo.ubi.jastec.packet;

import com.modusgo.ubi.jastec.IPacket;
import com.modusgo.ubi.jastec.IParser;
import com.modusgo.ubi.jastec.PacketParser;
import com.modusgo.ubi.jastec.Protocol;
import com.modusgo.ubi.jastec.Protocol.PacketType;
import com.modusgo.ubi.jastec.Utils;

public class DrivingSupportWhole implements IPacket, IParser{
	public static final int DRIVING_GROUP_SIZE = 16;
	
	public static final int INDEX_TODAY = 0;
	public static final int INDEX_IGNITION_ON = 1;
	public static final int INDEX_CURRENT = 2;
	
	public int DataBlockLength;
	public String SupportBit;
	public boolean[][] Group;
	public boolean HasSupport;
	
	public void init()
	{
		DataBlockLength = 0;
		SupportBit = "";
		HasSupport = false;
		if(Group == null)
		{
			Group = new boolean[3][DRIVING_GROUP_SIZE];	
		}
		
		for(int i = 0;i < Group.length; i++)
			for(int j = 0;j < Group[i].length; j++)
				Group[i][j] = false;
	}
	
	@Override
	public void parse(Byte[] packet) {
		PacketParser.getInstance().init(packet);
		
		DataBlockLength = PacketParser.getInstance().getLength();
		if(DataBlockLength == 0)
			return;
		
		Byte[] data = PacketParser.getInstance().getDataBlock();
		String groupBit = Utils.toBinaryString(data[1]);
		groupBit += Utils.toBinaryString(data[0]);
		
		
		// upper 4bit : support bit
		//byte supportValue = (byte)(data[3] >> 4);
		String supportBit = Utils.toBinaryString(data[3]);
		SupportBit = supportBit;
		HasSupport = !(Integer.parseInt(supportBit, 2) == 0);
			
		for(int i = 0;i < 4 ;i++)
		{
			if(i == 0 || supportBit.charAt(i) == '0')	
				continue;			
			
			for(int j = groupBit.length() - 1,k = 0;j >= 0; j--, k++)
			{
				if(groupBit.charAt(j) == '1')
					Group[i-1][k] = true;
				else if(groupBit.charAt(j) == '0')
					Group[i-1][k] = false;
			}
		}
	}
	
	public byte[] buildSupportByte(String supportBit, int specification)
	{
		String group1_9 = "";
		String group10_16 = "";
		
		for(int i = 7; i >= 0; i--)
		{
			if( Group[specification][i] )
			{
				group1_9 += "1";
			}
			else
			{
				group1_9 += "0";
			}
		}
		
		for(int i = DRIVING_GROUP_SIZE - 1; i >= 8; i--)
		{
			if( Group[specification][i] )
			{
				group10_16 += "1";
			}
			else
			{
				group10_16 += "0";
			}
		}
		
		// 0111 0000
		supportBit += "0000";
		
		// 1011 1111 ==> 1111 1011
		int group1 = Integer.valueOf(group1_9, 2);
		int group2 = Integer.valueOf(group10_16, 2);
		int support = Integer.valueOf(supportBit, 2);
		
		byte bSupport = (byte)support;
		byte bGroup1 = (byte)group1;
		byte bGroup2 = (byte)group2;		
		byte[] result = new byte[4];
		result[0] = bGroup1;
		result[1] = bGroup2;
		result[2] = 0x00;
		result[3] = bSupport;
		
		return result;
	}

	@Override
	public PacketType getPacketType() {
		return Protocol.PacketType.DRIVING_SUPPORT_WHOLE;
	}
}
