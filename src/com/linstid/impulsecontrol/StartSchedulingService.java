package com.linstid.impulsecontrol;

import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class StartSchedulingService extends IntentService {
	public final static String DEBUG_TAG = "StartSchedulingService";

	public StartSchedulingService() {
		super("StartSchedulingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ImpulseControlApplication.setLightPulseValue(ImpulseControlApplication.checkCurrentLightPulseValue(getSharedPreferences(ImpulseControlApplication.PREFS_NAME, Context.MODE_PRIVATE)));
                StartAlarmReceiver.completeWakefulIntent(intent);
	}
}
