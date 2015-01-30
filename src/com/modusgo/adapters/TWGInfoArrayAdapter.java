/**
 * 
 */
package com.modusgo.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.modusgo.ubi.DiagnosticsTroubleCode;
import com.modusgo.ubi.R;
import com.modusgo.ubi.Recall;
import com.modusgo.ubi.utils.Maintenance;
import com.modusgo.ubi.utils.ServicePerformed;
import com.modusgo.ubi.utils.TWGListItem;
import com.modusgo.ubi.utils.TWGListItem.twg_list_item_type;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author yaturner
 *
 */
public class TWGInfoArrayAdapter extends ArrayAdapter<TWGListItem>
{

	private Context context = null;
	private ArrayList<TWGListItem> objects = null;

	public TWGInfoArrayAdapter(Context context, int textViewResourceId, ArrayList<TWGListItem> objects)
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.twg_info_list_item, parent, false);
		Resources resources = context.getResources();
		TWGListItem item = objects.get(position);
		RelativeLayout hdr_view = (RelativeLayout) rowView.findViewById(R.id.hdr_view);
		RelativeLayout vehicle_info_view = (RelativeLayout) rowView.findViewById(R.id.vehicle_info_view);
		RelativeLayout recall_info_view = (RelativeLayout) rowView.findViewById(R.id.recall_info_view);
		LinearLayout dtc_info_view = (LinearLayout) rowView.findViewById(R.id.dtc_info_view);
		RelativeLayout alert_info_view = (RelativeLayout) rowView.findViewById(R.id.alert_info_view);
		LinearLayout service_info_view = (LinearLayout) rowView.findViewById(R.id.service_info_view);
		LinearLayout service_log_info_view = (LinearLayout) rowView.findViewById(R.id.service_log_info_view);

		hdr_view.setVisibility(View.GONE);
		vehicle_info_view.setVisibility(View.GONE);
		recall_info_view.setVisibility(View.GONE);
		dtc_info_view.setVisibility(View.GONE);
		alert_info_view.setVisibility(View.GONE);
		service_info_view.setVisibility(View.GONE);
		service_log_info_view.setVisibility(View.GONE);

		Recall recall = null;
		DiagnosticsTroubleCode dtc = null;
		Maintenance maintenance = null;
		ServicePerformed serviceEntry = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String text = null;

		switch (item.type)
		{
		case li_vehicle_info_hdr:
		case li_recall_hdr:
		case li_dtc_hdr:
		case li_alert_hdr:
		case li_service_log_hdr:
			hdr_view.setVisibility(View.VISIBLE);
			TextView hdr = (TextView) rowView.findViewById(R.id.hdr);

			if(item.type == twg_list_item_type.li_vehicle_info_hdr)
			{
				hdr.setText(resources.getString(R.string.VehicleInformation));
			} else if(item.type == twg_list_item_type.li_recall_hdr)
			{
				hdr.setText(resources.getString(R.string.RecallUpdate));
				hdr.setTextColor(resources.getColor(R.color.white));
				hdr.setBackgroundColor(resources.getColor(R.color.red));
			} else if(item.type == twg_list_item_type.li_dtc_hdr)
			{
				hdr.setText(resources.getString(R.string.DiagnosticsTroubleCodeDetails));
			} else if(item.type == twg_list_item_type.li_alert_hdr)
			{
				hdr.setText(resources.getString(R.string.Alerts));
			} else if(item.type == twg_list_item_type.li_service_log_hdr)
			{
				hdr.setText((String)item.value);
				hdr.setTextColor(0xFF000000);
			}
			break;
		case li_vehicle_info:
			// TextView info_name =
			// (TextView)rowView.findViewById(R.id.info_name);
			// TextView info_value =
			// (TextView)rowView.findViewById(R.id.info_value);
			//
			// vehicle_info_view.setVisibility(View.VISIBLE);
			// info_name.setText(item.value[0]);
			// info_value.setText(item.value[1]);
			break;
		case li_recall_info:
			recall = (Recall) item.value;
			TextView recall_name = (TextView) rowView.findViewById(R.id.recall_name);
			TextView recall_value = (TextView) rowView.findViewById(R.id.recall_value);

			recall_info_view.setVisibility(View.VISIBLE);
			recall_name.setText(recall.recall_id);
			recall_value.setText(recall.description);
			break;
		case li_dtc_info:
			dtc = (DiagnosticsTroubleCode) item.value;
			TextView dtc_name = (TextView) rowView.findViewById(R.id.dtc_name);
			TextView dtc_description = (TextView) rowView.findViewById(R.id.dtc_description);
			TextView dtc_priority = (TextView) rowView.findViewById(R.id.dtc_priority);

			dtc_info_view.setVisibility(View.VISIBLE);
			dtc_name.setText(dtc.code);
			dtc_description.setText(dtc.description);
			dtc_priority.setText(dtc.importance);
			break;

		// TODO - if there are no alerts show a green triangle
		case li_alert_info:
			dtc = (DiagnosticsTroubleCode) item.value;
			TextView alertInfo = (TextView) rowView.findViewById(R.id.alert_info);
			TextView alertDate = (TextView) rowView.findViewById(R.id.alert_date);

			alert_info_view.setVisibility(View.VISIBLE);
			String info = dtc.code + " - " + dtc.description;
			alertInfo.setText(info);
			alertInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alerts_red_big, 0, 0, 0);
			alertDate.setText(dtc.created_at);
			break;
		case li_service_item:
			maintenance = (Maintenance) item.value;
			TextView interval = (TextView) rowView.findViewById(R.id.service_detail_replace_every);
			TextView remaining = (TextView) rowView.findViewById(R.id.service_detail_next_service);
			TextView description = (TextView) rowView.findViewById(R.id.service_description);
			service_info_view.setVisibility(View.VISIBLE);
			description.setText(maintenance.description);
			text = context.getResources().getString(R.string.ReplaceEvery) + " " + maintenance.mileage + " miles";
			interval.setText(text);
			text = context.getResources().getString(R.string.NextServiceIn) + " 0 miles";
			remaining.setText(text);
			break;
		case li_service_log_item:
			serviceEntry = (ServicePerformed) item.value;
			TextView line1 = (TextView) rowView.findViewById(R.id.service_log_info_line1);
			TextView line2 = (TextView) rowView.findViewById(R.id.service_log_info_line2);
			service_log_info_view.setVisibility(View.VISIBLE);
			String dateString = sdf.format(serviceEntry.date_performed.getTime());
			line1.setText("Replaced on " + dateString + " at " + serviceEntry.location_performed);
			line2.setText("Milage was " + serviceEntry.milage_when_performed);
			break;
		}

		rowView.setTag(item);

		return rowView;

	}

}
