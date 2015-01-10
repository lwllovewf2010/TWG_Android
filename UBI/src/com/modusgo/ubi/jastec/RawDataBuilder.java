package com.modusgo.ubi.jastec;

import java.util.ArrayList;
import java.util.List;

public class RawDataBuilder {
	private ArrayList<Byte> mList;
	
	public void init()
	{
		mList = new ArrayList<Byte>();
	}
	
	public void add(byte[] packet)
	{
		for(byte b : packet)
		{
			mList.add(b);
		}
	}
	
	public Byte[][] build()
	{
		List<Byte[]> list = new ArrayList<Byte[]>();
		while(mList.contains(Protocol.CR))
		{
			int indexOfETX = mList.indexOf(Protocol.CR);
			Byte[] packet = mList.subList(0, indexOfETX + 1).toArray(new Byte[0]);
			list.add(packet);
			for(int i = 0;i < indexOfETX + 1; i++)
			{
				mList.remove(0);
			}
		}
		
		return list.toArray(new Byte[0][]);
	}
}
