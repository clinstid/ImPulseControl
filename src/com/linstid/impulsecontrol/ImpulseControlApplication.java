package com.linstid.impulsecontrol;

import android.app.Application;
import android.content.Context;

public class ImpulseControlApplication extends Application {
	private static Context appContext;
	
	// Preference name
	public static final String PREFS_NAME = "ImpulseControlPrefs";

	// Preference keys
	public static final String SCHEDULE_ENABLED_KEY = "scheduleEnabled";
	public static final String SCHEDULE_START_HOUR_KEY = "scheduleStartHour";
	public static final String SCHEDULE_START_MINUTE_KEY = "scheduleStartMinute";
	public static final String SCHEDULE_END_HOUR_KEY = "scheduleEndHour";
	public static final String SCHEDULE_END_MINUTE_KEY = "scheduleEndMinute";

	// Default to start sleep at 11:00PM and end at 7:00AM
	public static final int DEFAULT_START_HOUR = 23;
	public static final int DEFAULT_START_MINUTE = 0;
	public static final int DEFAULT_END_HOUR = 7;
	public static final int DEFAULT_END_MINUTE = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = getApplicationContext();
	}
	
	public static Context getContext() {
		return appContext;
	}
}
