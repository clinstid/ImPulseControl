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

public class StartAlarmReceiver extends WakefulBroadcastReceiver {
	private static final String DEBUG_TAG = "StartAlarmReceiver";
	private AlarmManager alarmMgr;
	private PendingIntent startAlarmIntent;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(DEBUG_TAG, "Received alarm!");
		Intent service = new Intent(context, StartSchedulingService.class);
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

		Intent startIntent = new Intent(context, StartAlarmReceiver.class);
		startAlarmIntent = PendingIntent.getBroadcast(context, 0, startIntent, 0);
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_START_HOUR_KEY,
				ImpulseControlApplication.DEFAULT_START_HOUR));
		start.set(Calendar.MINUTE, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY,
				ImpulseControlApplication.DEFAULT_START_MINUTE));
		start.set(Calendar.SECOND, 0);
		
		Calendar rightNow = Calendar.getInstance();
		if (rightNow.getTime().after(start.getTime())) {
			start.add(Calendar.DAY_OF_MONTH, 1);
		}

		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, start.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, startAlarmIntent);
		
		Log.d(DEBUG_TAG, "Set start[" + start.getTime() + "] repeating alarm.");

	}

	public void cancelAlarm(Context context) {
		if (alarmMgr != null) {
			alarmMgr.cancel(startAlarmIntent);
		}

		ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

	}

}
