package com.linstid.impulsecontrol;

import java.util.Calendar;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ImpulseControlApplication extends Application {
    private static Context appContext;

    private static final String DEBUG_TAG = "ImpulseControlApplication";

    // Preference name
    public static final String PREFS_NAME = "ImpulseControlPrefs";

    // Preference keys
    public static final String SCHEDULE_ENABLED_KEY = "scheduleEnabled";
    public static final String SCHEDULE_START_HOUR_KEY = "scheduleStartHour";
    public static final String SCHEDULE_START_MINUTE_KEY = "scheduleStartMinute";
    public static final String SCHEDULE_END_HOUR_KEY = "scheduleEndHour";
    public static final String SCHEDULE_END_MINUTE_KEY = "scheduleEndMinute";

    // Default to disable sleep and start sleep at 11:00PM and end at 7:00AM
    public static final boolean DEFAULT_SCHEDULE_ENABLED = false;
    public static final int DEFAULT_START_HOUR = 23;
    public static final int DEFAULT_START_MINUTE = 0;
    public static final int DEFAULT_END_HOUR = 7;
    public static final int DEFAULT_END_MINUTE = 0;

    public static final int LIGHT_PULSE_DISABLE = 0;
    public static final int LIGHT_PULSE_ENABLE = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getContext() {
        return appContext;
    }

    public static void setLightPulseValue() {
        SharedPreferences preferences = getContext().getSharedPreferences(
                PREFS_NAME, 0);

        Calendar rightNow = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY,
                preferences.getInt(SCHEDULE_START_HOUR_KEY, DEFAULT_START_HOUR));
        start.set(Calendar.MINUTE, preferences.getInt(
                SCHEDULE_START_MINUTE_KEY, DEFAULT_START_MINUTE));

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY,
                preferences.getInt(SCHEDULE_END_HOUR_KEY, DEFAULT_END_HOUR));
        end.set(Calendar.MINUTE,
                preferences.getInt(SCHEDULE_END_MINUTE_KEY, DEFAULT_END_MINUTE));

        Log.d(DEBUG_TAG, "Setting light pulse value - now[" + rightNow.getTime()
                + "] start[" + start.getTime() + "] end[" + end.getTime() + "]");

        if (rightNow.getTime().after(start.getTime())) {
            start.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (rightNow.getTime().after(end.getTime())) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }

        Date rightNowDate = rightNow.getTime();
        Date startDate = start.getTime();
        Date endDate = end.getTime();

        // In the off-chance that we happen to land right on the start or end
        // date, adjust up by 1 ms.
        if (rightNowDate.equals(startDate) || rightNowDate.equals(endDate)) {
            rightNowDate.setTime(rightNowDate.getTime() + 1);
        }

        int lightPulseValue;

        if (rightNowDate.after(startDate) && rightNowDate.before(endDate)) {
            lightPulseValue = LIGHT_PULSE_DISABLE;
            Log.d(DEBUG_TAG, "Disabling pulse notification light.");
        } else {
            lightPulseValue = LIGHT_PULSE_ENABLE;
            Log.d(DEBUG_TAG, "Enabling pulse notification light.");
        }

        android.provider.Settings.System.putInt(getContext()
                .getContentResolver(), "notification_light_pulse",
                lightPulseValue);
    }
}
