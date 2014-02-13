package com.linstid.impulsecontrol;

import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
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
		ImpulseControlApplication.setLightPulseValue(ImpulseControlApplication.checkCurrentLightPulseValue(getSharedPreferences(ImpulseControlApplication.PREFS_NAME, Context.MODE_PRIVATE)));
		EndAlarmReceiver.completeWakefulIntent(intent);
	}
}
