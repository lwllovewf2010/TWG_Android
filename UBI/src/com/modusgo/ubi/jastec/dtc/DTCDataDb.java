package com.modusgo.ubi.jastec.dtc;

import com.modusgo.ubi.jastec.Calenda_RTC;

public class DTCDataDb {

	//256
	public static final int SIZE = 256;
	
    public class SystemNameDB
    {
        public char[] SysName = new char[50];
    }

    public byte STX;
    public SystemNameDB SysID = new SystemNameDB();
    public SystemNameDB SysName = new SystemNameDB();
    public Calenda_RTC RTC = new Calenda_RTC();
    public byte DTCStructToNum;
    public short DTCType;
    public byte DTCSize;
    public byte FaultNum;
    public Byte[] FaultCode = new Byte[142];
    public byte ETX;
    
    public static DTCDataDb createDTCDataDb()
    {
    	return new DTCDataDb();
    }
}
