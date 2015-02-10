/**
 * 
 */
package com.modusgo.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import com.modusgo.twg.Constants;
import com.modusgo.twg.DiagnosticsTroubleCode;
import com.modusgo.twg.R;
import com.modusgo.twg.utils.Maintenance;
import com.modusgo.twg.utils.Recall;
import com.modusgo.twg.utils.ServicePerformed;
import com.modusgo.twg.utils.TWGListItem;
import com.modusgo.twg.utils.TWGListItem.twg_list_item_type;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
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
	private String dateString = null;

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
		LinearLayout recall_info_view = (LinearLayout) rowView.findViewById(R.id.recall_info_view);
		LinearLayout dtc_info_view = (LinearLayout) rowView.findViewById(R.id.dtc_info_view);
		LinearLayout alert_info_view = (LinearLayout) rowView.findViewById(R.id.alert_info_view);
		LinearLayout service_info_view = (LinearLayout) rowView.findViewById(R.id.service_info_view);
		LinearLayout service_due_info_view = (LinearLayout) rowView.findViewById(R.id.service_due_info_view);
		LinearLayout service_log_info_view = (LinearLayout) rowView.findViewById(R.id.service_log_info_view);

		hdr_view.setVisibility(View.GONE);
		vehicle_info_view.setVisibility(View.GONE);
		recall_info_view.setVisibility(View.GONE);
		dtc_info_view.setVisibility(View.GONE);
		alert_info_view.setVisibility(View.GONE);
		service_info_view.setVisibility(View.GONE);
		service_due_info_view.setVisibility(View.GONE);
		service_log_info_view.setVisibility(View.GONE);

		Recall recall = null;
		DiagnosticsTroubleCode dtc = null;
		Maintenance maintenance = null;
		ServicePerformed serviceEntry = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_TIME_FORMAT);
		SimpleDateFormat sdp = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
		String text = null;

		switch (item.type)
		{
		case li_vehicle_info_hdr:
		case li_recall_hdr:
		case li_dtc_hdr:
		case li_alert_hdr:
		case li_service_log_hdr:
		case li_alert_subhdr:
		case li_service_due_hdr:
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
				hdr.setText(resources.getString(R.string.DiagnosticsTroubleCodes));
			} else if(item.type == twg_list_item_type.li_alert_hdr)
			{
				hdr.setText(resources.getString(R.string.Alerts));
			} else if(item.type == twg_list_item_type.li_service_log_hdr)
			{
				hdr.setText((String)item.value);
				hdr.setTextColor(0xFF000000);
			} else if(item.type == twg_list_item_type.li_alert_subhdr)
			{
				hdr.setText((String)item.value);
				hdr.setTextColor(0xFF000000);
			}
			 else if(item.type == twg_list_item_type.li_service_due_hdr)
			{
					hdr.setText(resources.getString(R.string.VehicleInformation));
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
//			TextView recall_value = (TextView) rowView.findViewById(R.id.recall_value);
			TextView recall_date = (TextView) rowView.findViewById(R.id.recall_date);
			recall_info_view.setVisibility(View.VISIBLE);
			if(recall.created_at.length()>0)
			{
				try
				{
					dateString = sdf.format(sdp.parse(recall.created_at));
				} catch(ParseException e)
				{
					dateString = "N/A";
				}
			}
			else
			{
				dateString = "N/A";
			}
			String name = recall.recall_id + " - " + recall.description;
			recall_date.setText(dateString);
			recall_name.setText(name);
//			recall_value.setText(recall.description);
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

		case li_alert_info:
			dtc = (DiagnosticsTroubleCode) item.value;
			TextView alertDate = (TextView) rowView.findViewById(R.id.alert_date);
			TextView alertId = (TextView) rowView.findViewById(R.id.alert_id);
			TextView alertInfo = (TextView) rowView.findViewById(R.id.alert_info);

			alert_info_view.setVisibility(View.VISIBLE);
			if(dtc.created_at.length()>0)
			{
				try
				{
					dateString = sdf.format(sdp.parse(dtc.created_at));
				} catch(ParseException e)
				{
					dateString = "N/A";
				}
			}
			else
			{
				dateString = "N/A";
			}
			alertDate.setText(dateString);
			alertId.setText(dtc.code);
			alertInfo.setText((dtc.description.length()>0)?dtc.description:"N/A");
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
			text = context.getResources().getString(R.string.NextServiceIn) +  " " +maintenance.countdown + " miles";
			remaining.setText(text);
			break;
		case li_service_due_item:
			maintenance = (Maintenance) item.value;
			TextView due_remaining = (TextView) rowView.findViewById(R.id.service_due_remaining);
			TextView due_description = (TextView) rowView.findViewById(R.id.service_due_description);
			service_due_info_view.setVisibility(View.VISIBLE);
			due_description.setText(maintenance.description);
			//due_remaining.setText(text);  //demo only - this is hard coded in the XML
			break;
		case li_service_log_item:
			serviceEntry = (ServicePerformed) item.value;
			TextView line1 = (TextView) rowView.findViewById(R.id.service_log_info_line1);
			TextView line2 = (TextView) rowView.findViewById(R.id.service_log_info_line2);
			service_log_info_view.setVisibility(View.VISIBLE);
			String dateString = serviceEntry.date_performed;
			line1.setText("Replaced on " + dateString + " at " + serviceEntry.location_performed);
			line2.setText("Milage was " + serviceEntry.milage_when_performed);
			break;
		}

		rowView.setTag(item);

		return rowView;

	}

}
