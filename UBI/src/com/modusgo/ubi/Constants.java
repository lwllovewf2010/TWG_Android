package com.modusgo.ubi;

public class Constants {
	
	public static final String HOCKEY_APP_ID = "caca040742c8345b8594ac0bd7a71418";
	
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
	
	public static final String PREF_CURRENT_DRIVER = "currentDriver";
	public static final String PREF_AUTH_KEY = "auth_key";
	public static final String PREF_CLIENT_ID = "clientId";
	public static final String PREF_SHOW_TIP_POPUP = "showTipPopup";
	public static final String PREF_WELCOME_PAGES = "welcomePagesIds";
	public static final String PREF_ROLE = "role";
	public static final String PREF_DEVICE_MEID = "deviceMEID";
	public static final String PREF_DEVICE_TYPE = "deviceType";
	public static final String PREF_DEVICE_DATA_URL = "deviceDataUrl";
	public static final String PREF_DEVICE_AUTH_KEY = "deviceAuthKey";
	public static final String PREF_DIAGNOSTICS_CHECKUP_DATE = "diagnisticsCheckupDate";
	public static final String PREF_DIAGNOSTICS_STATUS = "diagnisticsStatus";
	
	public static final String ROLE_CUSTOMER = "customer";

	public static final String API_BASE_URL_PREFIX = "http://api.";
	public static final String API_BASE_URL_POSTFIX = ".test.modusgo.com/";
	public static final String API_PLATFORM = "android";
	
	//---------------------- DD -----------------------
	public static final String PREF_DD_ENABLED = "ddEnabled";
	public static final String API_AUTH_LOGIN = "ddclient";
	public static final String API_AUTH_PASS = "h7di3g$fi2g";
	public static final String DD_API_BASE_URL = "https://api.modusgo.com/dd/v1";
	public static final int CHECK_IGNITION_FREQUENCY = 30000;
	public static final int CHECK_IGNITION_NOT_AVAILABLE_TIME_LIMIT = 120000;
	public static final String INTENT_ACTION_UPDATE_MAIN = "com.modusgo.dd.UPDATE";
	public static final String REGISTRATION_BY_CODE_URL = DD_API_BASE_URL+"/regcode_register";
	public static final String PREF_REG_CODE = "reg_code";
	
    public static String getSendStatisticsURL(String mobile_id){
		return DD_API_BASE_URL+"/"+mobile_id+"/send_stat";    	
    }
    public static String getCheckIgnitionURL(String mobile_id){
		return DD_API_BASE_URL+"/"+mobile_id+"/ignition_status";    	
    }

}
