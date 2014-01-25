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
import android.widget.Toast;

public class Scheduler extends Activity {
    private final Context context = this;
    private StartAlarmReceiver startAlarm = new StartAlarmReceiver();
    private EndAlarmReceiver endAlarm = new EndAlarmReceiver();

    public static final String DEBUG_TAG = "Scheduler";

    private boolean scheduleEnabled;

    public boolean isScheduleEnabled() {
        return scheduleEnabled;
    }

    public int getCurrentDisplaySetting() throws SettingNotFoundException {
        return android.provider.Settings.System.getInt(
                context.getContentResolver(), "notification_light_pulse");
    }

    public void setScheduleEnabled(boolean scheduleEnabled) {
        if (this.scheduleEnabled == scheduleEnabled) {
            return;
        }

        this.scheduleEnabled = scheduleEnabled;
        Log.d(DEBUG_TAG, "Schedule enabled = " + isScheduleEnabled());

        updateCheckBox();
        updateScheduleDisplay();
    }

    private void displayDeviceNotSupportedAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder
                .setTitle("Device not supported")
                .setMessage(
                        "Your device does not have the required Android system setting to change the pulse notification light setting. Application will now exit.")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Scheduler.this.finish();
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
    public void savePreferencesButtonHandler(View view) {
        savePreferences();
    }

    public void savePreferences() {
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

        SharedPreferences preferences = getSharedPreferences(ImpulseControlApplication.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ImpulseControlApplication.SCHEDULE_ENABLED_KEY, isScheduleEnabled());
        editor.putInt(ImpulseControlApplication.SCHEDULE_START_HOUR_KEY, getScheduleStartHour());
        editor.putInt(ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY, getScheduleStartMinute());
        editor.putInt(ImpulseControlApplication.SCHEDULE_END_HOUR_KEY, getScheduleEndHour());
        editor.putInt(ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY, getScheduleEndMinute());
        editor.commit();

        if (isScheduleEnabled()) {
            // Clear existing alarms before we set new ones.
            startAlarm.cancelAlarm(this);
            endAlarm.cancelAlarm(this);
            startAlarm.setAlarm(this);
            endAlarm.setAlarm(this);
        } else {
            startAlarm.cancelAlarm(this);
            endAlarm.cancelAlarm(this);
        }

        Context context = getApplicationContext();
        CharSequence text = "Saved preferences.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        ImpulseControlApplication.setLightPulseValue();
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

        SharedPreferences preferences = getSharedPreferences(
                ImpulseControlApplication.PREFS_NAME, 0);
        setScheduleEnabled(preferences.getBoolean(
                ImpulseControlApplication.SCHEDULE_ENABLED_KEY, ImpulseControlApplication.DEFAULT_SCHEDULE_ENABLED));
        setScheduleStartHour(preferences.getInt(
                ImpulseControlApplication.SCHEDULE_START_HOUR_KEY, ImpulseControlApplication.DEFAULT_START_HOUR));
        setScheduleStartMinute(preferences.getInt(
                ImpulseControlApplication.SCHEDULE_START_MINUTE_KEY, ImpulseControlApplication.DEFAULT_START_MINUTE));
        setScheduleEndHour(preferences.getInt(
                ImpulseControlApplication.SCHEDULE_END_HOUR_KEY, ImpulseControlApplication.DEFAULT_END_HOUR));
        setScheduleEndMinute(preferences.getInt(
                ImpulseControlApplication.SCHEDULE_END_MINUTE_KEY, ImpulseControlApplication.DEFAULT_END_MINUTE));

        CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enableSchedulerCheckBox);
        enableCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        setScheduleEnabled(isChecked);
                        savePreferences();
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
