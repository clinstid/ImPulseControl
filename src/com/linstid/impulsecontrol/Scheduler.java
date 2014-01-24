package com.linstid.impulsecontrol;

import java.util.Calendar;
import com.linstid.impulsecontrol.R;

import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;

public class Scheduler extends Activity {
	private final Context context = this;
	private StartAlarmReceiver startAlarm = new StartAlarmReceiver();
	private EndAlarmReceiver endAlarm = new EndAlarmReceiver();

	public static final String DEBUG_TAG = "Scheduler";

	private SharedPreferences preferences;

	public SharedPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	private boolean scheduleEnabled;

	public boolean isScheduleEnabled() {
		return scheduleEnabled;
	}

	public int getCurrentDisplaySetting() throws SettingNotFoundException {
		return android.provider.Settings.System.getInt(
				context.getContentResolver(), "notification_light_pulse");
	}

	public void setScheduleEnabled(boolean scheduleEnabled) {
		this.scheduleEnabled = scheduleEnabled;
		updateCheckBox();
		updateScheduleDisplay();
		Log.d(DEBUG_TAG, "Schedule enabled = " + isScheduleEnabled());

		int newValue = scheduleEnabled ? 1 : 0;
		int previousValue = -1;
		try {
			previousValue = getCurrentDisplaySetting();
		} catch (SettingNotFoundException e) {
			displayDeviceNotSupportedAlert();
			previousValue = -1;
		}
		if (previousValue >= 0 && newValue != previousValue) {
			android.provider.Settings.System.putInt(
					context.getContentResolver(), "notification_light_pulse",
					newValue);
		}
	}

	private void displayDeviceNotSupportedAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder
				.setTitle("Device not supported")
				.setMessage(
						"Your device does not have the required Android system setting to change the pulse notification light setting. Do you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Scheduler.this.finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	public void updateCheckBox() {
		final CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enableSchedulerCheckBox);
		enableCheckBox.setChecked(scheduleEnabled);
	}

	public void setStartAlarm(Context context) {
		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.HOUR_OF_DAY, getScheduleStartHour());
		updateTime.set(Calendar.MINUTE, getScheduleStartMinute());
	}

	public void setEndAlarm(Context context) {
		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.HOUR_OF_DAY, getScheduleEndHour());
		updateTime.set(Calendar.MINUTE, getScheduleEndMinute());
	}

	// Called when the Save button is clicked.
	public void savePreferences(View view) {
		// Grab the current values from the TimePickers
		TimePicker startTimePicker = (TimePicker) findViewById(R.id.scheduleStartTimePicker);
		TimePicker endTimePicker = (TimePicker) findViewById(R.id.scheduleEndTimePicker);
		setScheduleStartHour(startTimePicker.getCurrentHour());
		setScheduleStartMinute(startTimePicker.getCurrentMinute());
		setScheduleEndHour(endTimePicker.getCurrentHour());
		setScheduleEndMinute(endTimePicker.getCurrentMinute());

		Log.d(DEBUG_TAG, "Saving preferences: " + ImpulseControlApplication.SCHEDULE_ENABLED_KEY + "["
				+ isScheduleEnabled() + "] " + "Start["
				+ getScheduleStartHour() + ":" + getScheduleStartMinute()
				+ "] End[" + getScheduleEndHour() + ":"
				+ getScheduleEndMinute() + "]");

		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(ImpulseControlApplication.SCHEDULE_ENABLED_KEY, isScheduleEnabled());
		editor.putInt(ImpulseControlApplication.SCHEDULE_START_HOUR_KEY, getScheduleStartHour());
		editor.putInt(ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY, getScheduleStartMinute());
		editor.putInt(ImpulseControlApplication.SCHEDULE_END_HOUR_KEY, getScheduleEndHour());
		editor.putInt(ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY, getScheduleEndMinute());
		editor.commit();

		if (isScheduleEnabled()) {
			startAlarm.setAlarm(this);
			endAlarm.setAlarm(this);
		}
		else {
			startAlarm.cancelAlarm(this);
			endAlarm.cancelAlarm(this);
		}
	}

	public void updateTimePickers() {
		TimePicker startTimePicker = (TimePicker) findViewById(R.id.scheduleStartTimePicker);
		startTimePicker.setCurrentHour(getScheduleStartHour());
		startTimePicker.setCurrentMinute(getScheduleStartMinute());

		TimePicker endTimePicker = (TimePicker) findViewById(R.id.scheduleEndTimePicker);
		endTimePicker.setCurrentHour(getScheduleEndHour());
		endTimePicker.setCurrentMinute(getScheduleEndMinute());
	}

	public void updateScheduleDisplay() {
		int newView = View.GONE;

		if (isScheduleEnabled()) {
			newView = View.VISIBLE;
		}

		LinearLayout scheduleLayout = (LinearLayout) findViewById(R.id.scheduleStartAndEndScrollViewLinearLayout);
		for (int i = 0; i < scheduleLayout.getChildCount(); i++) {
			View view = scheduleLayout.getChildAt(i);
			view.setVisibility(newView);
		}
	}

	private int scheduleStartHour = 0;
	private int scheduleStartMinute = 0;
	private int scheduleEndHour = 0;
	private int scheduleEndMinute = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scheduler);

		// This call has the side effect of displaying an alert if the device
		// doesn't support the system setting we need to control the
		// notification light.
		//
		// Tested on Galaxy Nexus:
		// android.provider.Settings.System ... "notification_light_pulse",
		//
		// TODO: Find and test some other devices.
		try {
			int previous = getCurrentDisplaySetting();
		} catch (SettingNotFoundException e) {
			displayDeviceNotSupportedAlert();
		}

		setPreferences(getSharedPreferences(ImpulseControlApplication.PREFS_NAME, 0));
		setScheduleEnabled(getPreferences().getBoolean(ImpulseControlApplication.SCHEDULE_ENABLED_KEY,
				false));
		setScheduleStartHour(getPreferences()
				.getInt(ImpulseControlApplication.SCHEDULE_START_HOUR_KEY, 0));
		setScheduleStartMinute(getPreferences().getInt(
				ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY, 0));
		setScheduleEndHour(getPreferences().getInt(ImpulseControlApplication.SCHEDULE_END_HOUR_KEY, 0));
		setScheduleEndMinute(getPreferences()
				.getInt(ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY, 0));

		CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enableSchedulerCheckBox);
		enableCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setScheduleEnabled(isChecked);
					}
				});

		updateTimePickers();
	}

	public int getScheduleStartHour() {
		return scheduleStartHour;
	}

	public void setScheduleStartHour(int scheduleStartHour) {
		this.scheduleStartHour = scheduleStartHour;
	}

	public int getScheduleStartMinute() {
		return scheduleStartMinute;
	}

	public void setScheduleStartMinute(int scheduleStartMinute) {
		this.scheduleStartMinute = scheduleStartMinute;
	}

	public int getScheduleEndHour() {
		return scheduleEndHour;
	}

	public void setScheduleEndHour(int scheduleEndHour) {
		this.scheduleEndHour = scheduleEndHour;
	}

	public int getScheduleEndMinute() {
		return scheduleEndMinute;
	}

	public void setScheduleEndMinute(int scheduleEndMinute) {
		this.scheduleEndMinute = scheduleEndMinute;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scheduler, menu);
		return true;
	}

}
