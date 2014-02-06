package com.linstid.impulsecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	StartAlarmReceiver startAlarm = new StartAlarmReceiver();
	EndAlarmReceiver endAlarm = new EndAlarmReceiver();

	@Override
	public void onReceive(Context context, Intent intent) {
		// Schedule alarms
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			startAlarm.setAlarm(context);
			endAlarm.setAlarm(context);
		}
		
		// Now figure out if the notification light is supposed to be enabled or
		// disabled.
		ImpulseControlApplication.setLightPulseValue();

	}

}
