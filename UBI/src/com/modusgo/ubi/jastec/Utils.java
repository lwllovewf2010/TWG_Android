package com.modusgo.ubi.jastec;

import java.util.Calendar;

import com.modusgo.ubi.Constants;

import android.content.SharedPreferences;

public class Utils {
	
	public static byte[] toBytes(Byte[] objectBytes)
	{
		byte[] bytes = new byte[objectBytes.length];
		
		for(int i = 0;i < objectBytes.length; i++)
		{
			bytes[i] = objectBytes[i].byteValue();
		}
		
		return bytes;
	}
	
	public static boolean storedDevice(SharedPreferences pref)
	{
		String deviceAddress = pref.getString(Constants.PREF_JASTEC_ADDRESS, "");
		return (!deviceAddress.equals(""));
	}
	
	public static short toLittleEndian(short data)
    {
		short result = (short)((data << 8) | (data >> 8));
        return result;
    }

	public static short toBigEndian(short data)
    {
        short result = (short)((data << 8) | (data >> 8));
        return result;
    }       
	
	public static byte reverse4Bit(byte value)
	{
		byte result = 0x00;
		
		result = (byte)((value & 0x0F) * 0x10);
		result += (byte)((value >> 4) & 0x0F);
		
		return result;
	}
	
	public static String toBinaryString(byte value)
	{	
		int temp = (byte)value & 0xFF;
		int zeroCount = Integer.numberOfLeadingZeros(temp);
		
		String binary = "";
		for(int i = 0;i < zeroCount - 24;i++)
		{
			binary += "0";
		}
		
		if(temp != 0)
			binary += Integer.toBinaryString(temp);
		
		return binary;
	}
	
	public static void reverse(Byte[] array)
	{
		Byte[] cloneArray = new Byte[array.length];
		for(int i = 0;i < array.length;i++)
			cloneArray[i] = array[i];
		
		for(int i = 0 , j = array.length - 1;i < array.length; i++, j--)
		{
			array[i] = cloneArray[j];
		}
	}
	
	public static short toShort(Byte[] value, int startIndex)
	{
		int shortSize = (Short.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[shortSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		short result = (short)((buffer[0] << 8) | (buffer[1] & 0x00FF));
		
		return result;
	}
	
	public static int toUShort(Byte[]value, int startIndex)
	{
		String temp = "";
		int shortSize = (Short.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[shortSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		for(int i = 0;i < shortSize;i++)
		{
			temp += String.format("%02X", buffer[i]);
		}
		
		
		int result = Integer.valueOf(temp, 16);
		
		return result;
	}
	
	public static long toUInt(Byte[] value, int startIndex)
	{
		String temp = "";
		int intSize = (Integer.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[intSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		for(int i = 0;i < intSize;i++)
		{
			temp += String.format("%02X", buffer[i]);
		}
		
		long result = Long.valueOf(temp, 16);
		
		return result;
	}
	
	public static int toInt(Byte[] value, int startIndex)
	{
//		String temp = "";
		int intSize = (Integer.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[intSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		
		
		Utils.reverse(buffer);
		
//		for(int i = 0;i < intSize;i++)
//		{
//			temp += String.format("%02X", buffer[i]);
//		}		
		
//		int result = Integer.valueOf(temp, 16);
		int result = (int)(	(buffer[0] << 8 * 3) | 
							((buffer[1] << 8 * 2) & 0x00FF0000) | 
							((buffer[2] << 8 * 1) & 0x0000FF00) | 
							(buffer[3] & 0x000000FF)	);
		
		return result;
	}
	
	public static long toLong4Byte(Byte[] value, int startIndex)
	{
		String temp = "";
		int intSize = (Integer.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[intSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		for(int i = 0;i < intSize;i++)
		{
			temp += String.format("%02X", buffer[i]);
		}
		
		long result = Long.valueOf(temp, 16);
		
		return result;
	}
	
	public static double toDouble(byte[] value, int startIndex)
	{
		String temp = "";
		int intSize = (Double.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[intSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		for(int i = 0;i < intSize;i++)
		{
			temp += String.format("%02X", buffer[i]);
		}
		
		long lValue = Long.valueOf(temp, 16);
		double result = Double.longBitsToDouble(lValue);
		
		return result;
	}
	
	public static float toFloat(Byte[] value, int startIndex)
	{
		String temp = "";
		int intSize = (Float.SIZE / Byte.SIZE);
		Byte[] buffer = new Byte[intSize];
		
		System.arraycopy(value, startIndex, buffer, 0, buffer.length);
		Utils.reverse(buffer);
		
		for(int i = 0;i < intSize;i++)
		{
			temp += String.format("%02X", buffer[i]);
		}
		
		long lValue = Long.parseLong(temp, 16);
		//int iValue = Integer.parseInt(temp, 16);
		float fValue = Float.intBitsToFloat((int)lValue);
		
		return fValue;
	}
	
	public static String convertMSToTime(long millisec)
	{
		int hour = (int)millisec / (1000*60*60);
		int minute = (int)((millisec % (1000*60*60)) / (1000*60));
		int sec = (int)((millisec % (1000*60*60)) % (1000*60) / 1000);
		
		String result = String.format("%02d", hour);
		result += ":";
		result += String.format("%02d", minute);
		result += ":";
		result += String.format("%02d", sec);
		
		return result;
	}
	
	public static String parseDTCCode(String binary)
	{
		String code = "";
		String sub = binary.substring(0, 2);
		
		if(sub.equals("00"))
			code = "P";
		else if(sub.equals("01"))
			code = "C";
		else if(sub.equals("10"))
			code = "B";
		else if(sub.equals("11"))
			code = "U";
		
		sub = binary.substring(2, 4);
		
		int iValue = Integer.parseInt(sub, 2);
		code += String.format("%d", iValue);
		
		sub = binary.substring(4, 8);
		iValue = Integer.parseInt(sub, 2);
		code += String.format("%d", iValue);
		
		sub = binary.substring(8, 12);
		iValue = Integer.parseInt(sub, 2);
		code += String.format("%d", iValue);
		
		sub = binary.substring(12, 16);
		iValue = Integer.parseInt(sub, 2);
		code += String.format("%d", iValue);
		
		return code;
	}
	
	public static byte[] createReadDataBlock(int address, short size)
	{
		byte[] readDataBlock = new byte[6];
		readDataBlock[0] = (byte)(address >> 8 * 3);
		readDataBlock[1] = (byte)(address >> 8 * 2);
		readDataBlock[2] = (byte)(address >> 8 * 1);
		readDataBlock[3] = (byte)(address & 0x000000FF);
		readDataBlock[4] = (byte)(size >> 8);
		readDataBlock[5] = (byte)(size & 0x00FF);
		
		return readDataBlock;
	}
	
	public static byte convertFuelTypeToCode(String fuelType)
	{
		if(fuelType.equals("°¡¼Ö¸°") || fuelType.toLowerCase().equals("gasoline"))
		{
			return 0x01;
		}
		else if(fuelType.equals("µðÁ©") || fuelType.toLowerCase().equals("diesel"))
		{
			return 0x02;
		}
		else if(fuelType.equals("LPG"))
		{
			return 0x03;
		}
		
		return 0x00;
	}
	
	public static byte[] toReverseByte(int value)
	{
		byte[] bValues = new byte[4];
		bValues[3] = (byte)(value >> 8 * 3);
		bValues[2] = (byte)((value & 0x00FF0000) >> 8 * 2);
		bValues[1] = (byte)((value & 0x0000FF00) >> 8);
		bValues[0] = (byte)((value & 0x000000FF));
		
		return bValues;
	}
	
	public static byte[] toByte(int value)
	{
		byte[] bValues = new byte[4];
		bValues[0] = (byte)(value >> 8 * 3);
		bValues[1] = (byte)((value & 0x00FF0000) >> 8 * 2);
		bValues[2] = (byte)((value & 0x0000FF00) >> 8);
		bValues[3] = (byte)((value & 0x000000FF));
		
		return bValues;
	}
	
	public static byte[] toReverseByte(short value)
	{
		byte[] bValues = new byte[2];
		bValues[1] = (byte)(value >> 8);
		bValues[0] = (byte)((value & 0x00FF));
		
		return bValues;
	}
	
	public static byte[] toByte(short value)
	{
		byte[] bValues = new byte[2];
		bValues[0] = (byte)(value >> 8);
		bValues[1] = (byte)((value & 0x00FF));
		
		return bValues;
	}
}
