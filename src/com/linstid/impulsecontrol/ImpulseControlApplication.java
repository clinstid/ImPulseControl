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

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor prefEditor;

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public static void setScheduleEnabled(boolean enabled) {
        Log.d(DEBUG_TAG, "Setting " + SCHEDULE_ENABLED_KEY + " to " + enabled);
        prefEditor.putBoolean(SCHEDULE_ENABLED_KEY, enabled);
        prefEditor.commit();
    }

    public static boolean isScheduleEnabled() {
       return preferences.getBoolean(SCHEDULE_ENABLED_KEY, DEFAULT_SCHEDULE_ENABLED);
    }

    public static void setScheduleStartHour(int hour) {
        Log.d(DEBUG_TAG, "Setting " + SCHEDULE_START_HOUR_KEY + " to " + hour);
        prefEditor.putInt(SCHEDULE_START_HOUR_KEY, hour);
        prefEditor.commit();
    }

    public static int getScheduleStartHour() {
        return preferences.getInt(SCHEDULE_START_HOUR_KEY, DEFAULT_START_HOUR);
    }

    public static void setScheduleEndHour(int hour) {
        Log.d(DEBUG_TAG, "Setting " + SCHEDULE_END_HOUR_KEY + " to " + hour);
        prefEditor.putInt(SCHEDULE_END_HOUR_KEY, hour);
        prefEditor.commit();
    }

    public static int getScheduleEndHour() {
        return preferences.getInt(SCHEDULE_END_HOUR_KEY, DEFAULT_START_HOUR);
    }

    public static void setScheduleStartMinute(int minute) {
        Log.d(DEBUG_TAG, "Setting " + SCHEDULE_START_MINUTE_KEY + " to " + minute);
        prefEditor.putInt(SCHEDULE_START_MINUTE_KEY, minute);
        prefEditor.commit();
    }

    public static int getScheduleStartMinute() {
        return preferences.getInt(SCHEDULE_START_MINUTE_KEY, DEFAULT_START_MINUTE);
    }

    public static void setScheduleEndMinute(int minute) {
        Log.d(DEBUG_TAG, "Setting " + SCHEDULE_END_MINUTE_KEY + " to " + minute);
        prefEditor.putInt(SCHEDULE_END_MINUTE_KEY, minute);
        prefEditor.commit();
    }

    public static int getScheduleEndMinute() {
        return preferences.getInt(SCHEDULE_END_MINUTE_KEY, DEFAULT_START_MINUTE);
    }

    public static final int LIGHT_PULSE_DISABLE = 0;
    public static final int LIGHT_PULSE_ENABLE = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFS_NAME, 0);
        prefEditor = preferences.edit();
    }

    public static Context getContext() {
        return appContext;
    }

    public static int checkCurrentLightPulseValue(SharedPreferences preferences) {
        if (!preferences.getBoolean(SCHEDULE_ENABLED_KEY, false)) {
            return LIGHT_PULSE_ENABLE;
        }

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

        Date rightNowDate = rightNow.getTime();
        Date startDate = start.getTime();
        Date endDate = end.getTime();

        // In the off-chance that we happen to land right on the start or end
        // date, adjust up by 1 second.
        if (rightNowDate.equals(startDate) || rightNowDate.equals(endDate)) {
            rightNow.add(Calendar.SECOND, 1);
            rightNowDate = rightNow.getTime();
        }

        Log.d(DEBUG_TAG, "Setting light pulse value - now[" + rightNow.getTime()
                + "] start[" + start.getTime() + "] end[" + end.getTime() + "]");

        int lightPulseValue;

        Log.d(DEBUG_TAG, "now after start = " + rightNowDate.after(startDate) +
                " now before end = " + rightNowDate.before(endDate));

        // ^
        // | enabled
        // |-start
        // | disabled
        // |-end
        // | enabled
        // v
        if (startDate.before(endDate)) {
            if (rightNowDate.before(startDate) || rightNowDate.after(endDate)) {
                lightPulseValue = LIGHT_PULSE_ENABLE;
                Log.d(DEBUG_TAG, "Enabling pulse notification light.");
            }
            else {
                lightPulseValue = LIGHT_PULSE_DISABLE;
                Log.d(DEBUG_TAG, "Disabling pulse notification light.");
            }
        }
        // ^
        // | disabled
        // |-end
        // | enabled
        // |-start
        // | disabled
        // v
        else {
            if (rightNowDate.after(endDate) && rightNowDate.before(startDate)) {
                lightPulseValue = LIGHT_PULSE_ENABLE;
                Log.d(DEBUG_TAG, "Enabling pulse notification light.");
            }
            else {
                lightPulseValue = LIGHT_PULSE_DISABLE;
                Log.d(DEBUG_TAG, "Disabling pulse notification light.");
            }
        }

        return lightPulseValue;
    }

    public static void setLightPulseValue(int lightPulseValue) {
        android.provider.Settings.System.putInt(getContext()
                .getContentResolver(), "notification_light_pulse",
                lightPulseValue);
    }
}
