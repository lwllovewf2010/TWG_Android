package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    	Intent i = new Intent(context, CallSaverService.class);
    	i.putExtra("action", intent.getExtras().getString(TelephonyManager.EXTRA_STATE));
    	context.startService(i);
    }
}