package com.modusgo.ubi;

public class Constants {
	
	public static final String HOCKEY_APP_ID = "caca040742c8345b8594ac0bd7a71418";
	public static final String GCM_SENDER_ID = "322787790569";
	
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
	public static final String DATE_TIME_FORMAT_ZULU = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String DEFAULT_TIMEZONE = "GMT+00:00";
	
	public static final String PREF_CURRENT_DRIVER = "currentDriver";
	public static final String PREF_AUTH_KEY = "auth_key";
	public static final String PREF_CLIENT_ID = "clientId";
	public static final String PREF_SHOW_TIP_POPUP = "showTipPopup";
	public static final String PREF_WELCOME_PAGES = "welcomePagesIds";
	public static final String PREF_UNITS_OF_MEASURE = "unitsOfMeasure";
	public static final String PREF_DRIVER_ID = "id";
	public static final String PREF_VEHICLE_ID = "vehicle_id";
	public static final String PREF_FIRST_NAME = "first_name";
	public static final String PREF_LAST_NAME = "last_name";
	public static final String PREF_EMAIL = "email";
	public static final String PREF_ROLE = "role";
	public static final String PREF_PHONE = "phone_number";
	public static final String PREF_TIMEZONE = "time_zone";
	public static final String PREF_TIMEZONE_OFFSET = "time_zone_offset";
	public static final String PREF_PHOTO = "photo";
	public static final String PREF_DIAGNOSTIC = "diagnostic";
	public static final String PREF_DEVICE_MEID = "deviceMEID";
	public static final String PREF_DEVICE_TYPE = "deviceType";
	public static final String PREF_DEVICE_EVENTS = "deviceEvents";
	public static final String PREF_DEVICE_TRIPS = "deviceTrips";
	public static final String PREF_DEVICE_IN_TRIP = "deviceInTrip";
	public static final String PREF_DEVICE_DATA_URL = "deviceDataUrl";
	public static final String PREF_DEVICE_AUTH_KEY = "deviceAuthKey";
	public static final String PREF_EVENTS_LAST_CHECK = "eventsLastCheck";
	public static final String PREF_GA_TRACKING_ID = "ga_trackingId";
	public static final String PREF_GCM_REG_ID = "gmcRegId";
	public static final String PREF_APP_VERSION = "appVersion";
	public static final String PREF_CONTACT_PHONE = "contactPhone";
	public static final String PREF_AGENT_PHONE = "agentPhone";
	public static final String PREF_FIND_MECHANIC_ENABLED = "findMechanicEnabled";
	public static final String PREF_DTC_PRICES_ENABLED = "dtcPricesEnabled";
	public static final String PREF_MAINTENANCE_PRICES_ENABLED = "maintenancePricesEnabled";
	public static final String PREF_DIAGNOSTICS_DELETE_POPUP_SHOWED = "diagsDeletePopup";
	public static final String PREF_ALERTS_DELETE_POPUP_SHOWED = "alertsDeletePopup";
	
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
    
    //------------------ Branding ------------
	public static final String PREF_BR_LOGIN_SCREEN_BG_IMAGE = "login_bg_image";
	public static final String PREF_BR_LOGIN_SCREEN_LOGO = "login_logo";
	public static final String PREF_BR_BUTTONS_BG_COLOR = "buttons_color";
	public static final String PREF_BR_BUTTONS_TEXT_COLOR = "buttons_text_color";
	public static final String PREF_BR_TITLE_BAR_BG = "title_bar_bg_image";
	public static final String PREF_BR_TITLE_BAR_BG_COLOR = "title_bar_bg_color";
	public static final String PREF_BR_TITLE_BAR_TEXT_COLOR = "title_bar_text_color";
	public static final String PREF_BR_TITLE_BAR_BUTTONS_COLOR = "title_bar_buttons_color";
	public static final String PREF_BR_MENU_LOGO = "menu_logo";
	public static final String PREF_BR_SWITCH_DRIVER_MENU_BUTTON_COLOR = "switch_driver_color";
	public static final String PREF_BR_LIST_HEADER_LINE_COLOR = "list_header_line_color";
	
	public static final String BUTTON_BG_COLOR = "#f15b2a";
	public static final String BUTTON_TEXT_COLOR = "#edf1f9";
	public static final String SWITCH_DRIVER_BUTTON_BG_COLOR = "#f15b2a";
	public static final String TITLE_BAR_BG_COLOR = "#000000";
	public static final String TITLE_BAR_TEXT_COLOR = "#f15b2a";
	public static final String LIST_HEADER_LINE_COLOR = "#00aeef";
	public static final String TITLE_BAR_BUTTONS_COLOR = "#697078";
}
