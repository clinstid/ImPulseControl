package com.linstid.impulsecontrol;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class EndAlarmReceiver extends WakefulBroadcastReceiver {
	private static final String DEBUG_TAG = "EndAlarmReceiver";
	private AlarmManager alarmMgr;
	private PendingIntent endAlarmIntent;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(DEBUG_TAG, "Received alarm!");
		Intent service = new Intent(context, EndSchedulingService.class);
		Log.d(DEBUG_TAG, "Starting service @ " + SystemClock.elapsedRealtime());
		startWakefulService(context, service);
	}

	public void setAlarm(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				ImpulseControlApplication.PREFS_NAME, 0);

		// If schedule isn't enabled, return without setting any alarms.
		if (!preferences.getBoolean(ImpulseControlApplication.SCHEDULE_ENABLED_KEY, false)) {
			return;
		}

		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent endIntent = new Intent(context, EndAlarmReceiver.class);
		endAlarmIntent = PendingIntent.getBroadcast(context, 0, endIntent, 0);
		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_END_HOUR_KEY,
				ImpulseControlApplication.DEFAULT_END_HOUR));
		end.set(Calendar.MINUTE, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY,
				ImpulseControlApplication.DEFAULT_END_MINUTE));
		end.set(Calendar.SECOND, 0);
		
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, endAlarmIntent);
		
		Log.d(DEBUG_TAG, "Set end[" + end.getTime() + "] repeating alarm.");

	}

	public void cancelAlarm(Context context) {
		if (alarmMgr != null) {
			alarmMgr.cancel(endAlarmIntent);
		}

		ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

	}

}
