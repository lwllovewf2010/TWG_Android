package com.modusgo.dd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneScreenOnOffReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Intent i = new Intent(context, CallSaverService.class);
    	i.putExtra("action", intent.getAction());
    	context.startService(i);
    }
}