package com.linstid.impulsecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    public static final String DEBUG_TAG = "ImpulseControl::BootReceiver";
    StartAlarmReceiver startAlarm = new StartAlarmReceiver();
    EndAlarmReceiver endAlarm = new EndAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Schedule alarms
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(DEBUG_TAG, "Scheduling alarms.");
            startAlarm.setAlarm(context);
            endAlarm.setAlarm(context);
        }

        // Now figure out if the notification light is supposed to be enabled or
        // disabled.
        ImpulseControlApplication.setLightPulseValue(ImpulseControlApplication.checkCurrentLightPulseValue(context.getSharedPreferences(ImpulseControlApplication.PREFS_NAME, Context.MODE_PRIVATE)));
    }

}
