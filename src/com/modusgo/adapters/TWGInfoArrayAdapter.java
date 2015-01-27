/**
 * 
 */
package com.modusgo.adapters;

import java.util.ArrayList;

import com.modusgo.ubi.DiagnosticsTroubleCode;
import com.modusgo.ubi.R;
import com.modusgo.ubi.Recall;
import com.modusgo.ubi.utils.Maintenance;
import com.modusgo.ubi.utils.TWGListItem;
import com.modusgo.ubi.utils.TWGListItem.twg_list_item_type;

import android.content.Context;
import android.content.res.Resources;
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

	public TWGInfoArrayAdapter(Context context, int textViewResourceId,
			ArrayList<TWGListItem> objects)
	{
		super(context, textViewResourceId, objects);
		this.context = context;
		this.objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.twg_info_list_item, parent,
				false);
		Resources resources = context.getResources();
		TWGListItem item = objects.get(position);
		RelativeLayout hdr_view = (RelativeLayout) rowView
				.findViewById(R.id.hdr_view);
		RelativeLayout vehicle_info_view = (RelativeLayout) rowView
				.findViewById(R.id.vehicle_info_view);
		RelativeLayout recall_info_view = (RelativeLayout) rowView
				.findViewById(R.id.recall_info_view);
		LinearLayout dtc_info_view = (LinearLayout) rowView
				.findViewById(R.id.dtc_info_view);
		RelativeLayout alert_info_view = (RelativeLayout) rowView
				.findViewById(R.id.alert_info_view);
		LinearLayout service_log_info_view = (LinearLayout) rowView.findViewById(R.id.service_log_info_view);

		hdr_view.setVisibility(View.GONE);
		vehicle_info_view.setVisibility(View.GONE);
		recall_info_view.setVisibility(View.GONE);
		dtc_info_view.setVisibility(View.GONE);
		alert_info_view.setVisibility(View.GONE);
		service_log_info_view.setVisibility(View.GONE);
		

		Recall recall = null;
		DiagnosticsTroubleCode dtc = null;
		Maintenance maintenance = null;

		switch (item.type)
		{
		case li_vehicle_info_hdr:
		case li_recall_hdr:
		case li_dtc_hdr:
		case li_alert_hdr:
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
				hdr.setText(resources
						.getString(R.string.DiagnosticsTroubleCodeDetails));
			} else if(item.type == twg_list_item_type.li_alert_hdr)
			{
				hdr.setText(resources.getString(R.string.Alerts));
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
			TextView recall_name = (TextView) rowView
					.findViewById(R.id.recall_name);
			TextView recall_value = (TextView) rowView
					.findViewById(R.id.recall_value);

			recall_info_view.setVisibility(View.VISIBLE);
			recall_name.setText(recall.recall_id);
			recall_value.setText(recall.description);
			break;
		case li_dtc_info:
			dtc = (DiagnosticsTroubleCode) item.value;
			TextView dtc_name = (TextView) rowView.findViewById(R.id.dtc_name);
			TextView dtc_description = (TextView) rowView
					.findViewById(R.id.dtc_description);
			TextView dtc_priority = (TextView) rowView
					.findViewById(R.id.dtc_priority);

			dtc_info_view.setVisibility(View.VISIBLE);
			dtc_name.setText(dtc.code);
			dtc_description.setText(dtc.description);
			dtc_priority.setText(dtc.importance);
			break;

		// TODO - if there are no alerts show a green triangle
		case li_alert_info:
			dtc = (DiagnosticsTroubleCode) item.value;
			TextView alertInfo = (TextView) rowView
					.findViewById(R.id.alert_info);
			TextView alertDate = (TextView) rowView
					.findViewById(R.id.alert_date);

			alert_info_view.setVisibility(View.VISIBLE);
			String info = dtc.code + " - " + dtc.description;
			alertInfo.setText(info);
			alertInfo.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_alerts_red_big, 0, 0, 0);
			alertDate.setText(dtc.created_at);
			break;
		case li_service_log_item:
			maintenance = (Maintenance)item.value;
			TextView interval = (TextView) rowView.findViewById(R.id.service_log_replace_interval);
			TextView remaining = (TextView) rowView.findViewById(R.id.service_log_remaining);
			TextView description = (TextView) rowView.findViewById(R.id.service_log_description);
			service_log_info_view.setVisibility(View.VISIBLE);
			description.setText(maintenance.description);
		}

		rowView.setTag(item);

		return rowView;

	}

}
