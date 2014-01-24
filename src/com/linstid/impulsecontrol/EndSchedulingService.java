package com.linstid.impulsecontrol;

import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class EndSchedulingService extends IntentService {
	public final static String DEBUG_TAG = "EndSchedulingService";

	public EndSchedulingService() {
		super("EndSchedulingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences preferences = getSharedPreferences(
				ImpulseControlApplication.PREFS_NAME, 0);
		Calendar rightNow = Calendar.getInstance();
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_START_HOUR_KEY,
				ImpulseControlApplication.DEFAULT_START_HOUR));
		start.set(Calendar.MINUTE, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY,
				ImpulseControlApplication.DEFAULT_START_MINUTE));

		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_END_HOUR_KEY,
				ImpulseControlApplication.DEFAULT_END_HOUR));
		end.set(Calendar.MINUTE, preferences.getInt(
				ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY,
				ImpulseControlApplication.DEFAULT_END_MINUTE));

		Log.d(DEBUG_TAG, "Received alarm - now[" + rightNow.getTime()
				+ "] start[" + start.getTime() + "] end[" + end.getTime() + "]");

		Date rightNowDate = rightNow.getTime();
		Date startDate = start.getTime();
		Date endDate = end.getTime();
		
		// In the off-chance that we happen to land right on the start or end
		// date, adjust up by 1 ms.
		if (rightNowDate.equals(startDate) || rightNowDate.equals(endDate)) {
			rightNowDate.setTime(rightNowDate.getTime() + 1);
		}

		// First case where the start is before the end in the same day.
		if (startDate.before(endDate)) {
			if (rightNowDate.after(startDate) && rightNowDate.before(endDate)) {
				Log.d(DEBUG_TAG, "Sleeping");
			}
			else {
				Log.d(DEBUG_TAG, "Glowing");
			}
		}
		// Second case where the start is after the end in the same day.
		else
		{
			if (rightNowDate.after(startDate) || rightNowDate.before(endDate)) {
				Log.d(DEBUG_TAG, "Sleeping");
			}
			else {
				Log.d(DEBUG_TAG, "Glowing");
			}
		}

		EndAlarmReceiver.completeWakefulIntent(intent);
	}
}
