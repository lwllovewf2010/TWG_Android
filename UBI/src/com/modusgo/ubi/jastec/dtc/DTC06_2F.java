package com.modusgo.ubi.jastec.dtc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.modusgo.ubi.jastec.Utils;
import com.modusgo.ubi.jastec.packet.DTCRead.DTCItem;

public class DTC06_2F implements IDTCDescriptor{
	private MakerType mMakerType;
	private DTCDataDb mDTCDataDb;
	
	public DTC06_2F(DTCDataDb dtc)	
	{
		switch((dtc.DTCType & 0xF000))
		{
		case 0x0000:
			mMakerType = MakerType.HYUNDAI_KIA;
			break;
		case 0x1000:
			mMakerType = MakerType.DAEWOO;
			break;
		case 0x2000:
			mMakerType = MakerType.SAMSUNG;
			break;
		case 0x3000:
			mMakerType = MakerType.SSANGYONG;
			break;
		}
		mDTCDataDb = dtc;
	}
	
	@Override
	public boolean isThisMakerType(MakerType makerType) {
		
		return (mMakerType == makerType);
	}
	
	/**
	 * 3bytes : Dtc[2](2), status(1)
	 */

	@Override
	public DTCItem[] makeDTCItem() {
		String binCode = "";
		String strDTC = "";
		String strDTCDateTime = "";
		String strOtherDateTimeFormat = "";
		
		List<DTCItem> mList = new ArrayList<DTCItem>();
		
		DTCItem item = null;
		
		for(int i = 0, faultCodeIndex = 0;i < mDTCDataDb.FaultNum;i++, faultCodeIndex += 3)
		{
			binCode = Utils.toBinaryString(mDTCDataDb.FaultCode[faultCodeIndex]);
			binCode += Utils.toBinaryString(mDTCDataDb.FaultCode[faultCodeIndex + 1]);
			strDTC = Utils.parseDTCCode(binCode);
			
			Calendar calendar = Utils.convertRTCToCalendar(mDTCDataDb.RTC);
			strDTCDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
			strOtherDateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime());
			
			item = new DTCItem(strDTC, strDTCDateTime, strOtherDateTimeFormat);
			mList.add(item);
		}
		
		return mList.toArray(new DTCItem[0]);
	}

	@Override
	public int DTCCodeCount() {
		return mDTCDataDb.FaultNum;
	}
	
	@Override
	public boolean hasPending() {
		return (mDTCDataDb.DTCStructToNum == 2);
	}
	
}
