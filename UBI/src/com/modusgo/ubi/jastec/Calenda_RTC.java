package com.modusgo.ubi.jastec;

import android.os.Parcel;
import android.os.Parcelable;

public class Calenda_RTC implements Parcelable{
	public static final int SIZE = 7;
    public short Year;
    public byte Month;
    public byte Day;
    public byte Hour;
    public byte Min;
    public byte Sec;
    
//    public static Calenda_RTC parse(Byte[] data)
//    {
//    	Calenda_RTC rtc = new Calenda_RTC();
//    	
//    	Byte[] year = new Byte[2];
//        System.arraycopy(data, 0, year, 0, year.length);
//        rtc.Year 	= Utils.toShort(year, 0);
//        rtc.Month 	= data[2];
//        rtc.Day 	= data[3];
//        rtc.Hour 	= data[4];
//        rtc.Min 	= data[5];
//        rtc.Sec 	= data[6];
//        
//        return rtc;
//    }
    
    public void parse(Byte[] data)
    {
    	Byte[] year = new Byte[2];
        System.arraycopy(data, 0, year, 0, year.length);
        Year 	= Utils.toShort(year, 0);
        Month 	= data[2];
        Day 	= data[3];
        Hour 	= data[4];
        Min 	= data[5];
        Sec 	= data[6];
    }
    
    @Override
    public String toString()
    {
    	String temp = String.format("%02d-%02d-%02d %02d:%02d:%02d", Year, Month, Day, Hour, Min, Sec);
    	
    	return temp;
    }
    
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(Year);
		parcel.writeByte(Month);
		parcel.writeByte(Day);
		parcel.writeByte(Hour);
		parcel.writeByte(Min);
		parcel.writeByte(Sec);
	}
    
    private final Creator<Calenda_RTC> CREATOR = new Creator<Calenda_RTC>() {
		@Override
		public Calenda_RTC[] newArray(int i) {
			return new Calenda_RTC[i];
		}
		
		@Override
		public Calenda_RTC createFromParcel(Parcel parcel) {
			Calenda_RTC rtc = new Calenda_RTC();
			
			rtc.Year = (short)parcel.readInt();
			rtc.Month = parcel.readByte();
			rtc.Day = parcel.readByte();
			rtc.Hour = parcel.readByte();
			rtc.Min = parcel.readByte();
			rtc.Sec = parcel.readByte();
			
			return rtc;
		}
	};

}
