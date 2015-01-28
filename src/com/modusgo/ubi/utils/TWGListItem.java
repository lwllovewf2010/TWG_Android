package com.modusgo.ubi.utils;


public class TWGListItem {
	public static enum twg_list_item_type {
		li_vehicle_info_hdr, li_vehicle_info, li_recall_hdr, li_recall_info, li_dtc_hdr, li_dtc_info, li_alert_hdr, li_alert_info,
		li_service_item, li_service_log_item
	}

	public twg_list_item_type type;
	public Object value = null;

	public TWGListItem(twg_list_item_type type, Object value) {
		this.type = type;
		this.value = value;
	}
}


