package com.linstid.impulsecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class Scheduler extends Activity {
    private final Context context = this;
    private StartAlarmReceiver startAlarm = new StartAlarmReceiver();
    private EndAlarmReceiver endAlarm = new EndAlarmReceiver();

    public static final String DEBUG_TAG = "Scheduler";

    public int getCurrentDisplaySetting() throws SettingNotFoundException {
        return android.provider.Settings.System.getInt(
                context.getContentResolver(), "notification_light_pulse");
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

    public void setStartAlarm(Context context) {
        Calendar updateTime = Calendar.getInstance();
        updateTime.set(Calendar.HOUR_OF_DAY, ImpulseControlApplication.getScheduleStartHour());
        updateTime.set(Calendar.MINUTE, ImpulseControlApplication.getScheduleStartMinute());
    }

    public void setEndAlarm(Context context) {
        Calendar updateTime = Calendar.getInstance();
        updateTime.set(Calendar.HOUR_OF_DAY, ImpulseControlApplication.getScheduleEndHour());
        updateTime.set(Calendar.MINUTE, ImpulseControlApplication.getScheduleEndMinute());
    }

    // Called when the Save button is clicked.
    public void savePreferencesButtonHandler(View view) {
        savePreferences();
        updateDisplay();
    }

    public void savePreferences() {
        // Grab the current values from the TimePickers
        TimePicker startTimePicker = (TimePicker) findViewById(R.id.scheduleStartTimePicker);
        TimePicker endTimePicker = (TimePicker) findViewById(R.id.scheduleEndTimePicker);
        CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enableSchedulerCheckBox);

        ImpulseControlApplication.setScheduleEnabled(enableCheckBox.isChecked());
        ImpulseControlApplication.setScheduleStartHour(startTimePicker.getCurrentHour());
        ImpulseControlApplication.setScheduleStartMinute(startTimePicker.getCurrentMinute());
        ImpulseControlApplication.setScheduleEndHour(endTimePicker.getCurrentHour());
        ImpulseControlApplication.setScheduleEndMinute(endTimePicker.getCurrentMinute());

        Log.d(DEBUG_TAG, "Saving preferences: " + ImpulseControlApplication.SCHEDULE_ENABLED_KEY + "["
                + ImpulseControlApplication.isScheduleEnabled() + "] " + "Start["
                + ImpulseControlApplication.getScheduleStartHour() + ":" + ImpulseControlApplication.getScheduleStartMinute()
                + "] End[" + ImpulseControlApplication.getScheduleEndHour() + ":"
                + ImpulseControlApplication.getScheduleEndMinute() + "]");

        if (ImpulseControlApplication.isScheduleEnabled()) {
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

        ImpulseControlApplication.setLightPulseValue(ImpulseControlApplication.checkCurrentLightPulseValue(ImpulseControlApplication.getPreferences()));
    }

    public void updateTimePickers() {
        TimePicker startTimePicker = (TimePicker) findViewById(R.id.scheduleStartTimePicker);
        startTimePicker.setCurrentHour(ImpulseControlApplication.getScheduleStartHour());
        startTimePicker.setCurrentMinute(ImpulseControlApplication.getScheduleStartMinute());

        TimePicker endTimePicker = (TimePicker) findViewById(R.id.scheduleEndTimePicker);
        endTimePicker.setCurrentHour(ImpulseControlApplication.getScheduleEndHour());
        endTimePicker.setCurrentMinute(ImpulseControlApplication.getScheduleEndMinute());
    }

    public void updateDisplay() {
        // This call has the side effect of displaying an alert if the device
        // doesn't support the system setting we need to control the
        // notification light.
        //
        // Tested on Galaxy Nexus:
        // android.provider.Settings.System ... "notification_light_pulse",
        //
        // TODO: Find and test some other devices.
        int previous = 0;
        try {
            previous = getCurrentDisplaySetting();
        } catch (SettingNotFoundException e) {
            displayDeviceNotSupportedAlert();
        }

        TextView lightStatusTextView = (TextView) findViewById(R.id.notificationLightStatus);
        if (previous == ImpulseControlApplication.LIGHT_PULSE_ENABLE) {
            lightStatusTextView.setTextColor(Color.GREEN);
            lightStatusTextView.setText(R.string.notificationLightStatusEnabled);
        } else {
            lightStatusTextView.setTextColor(Color.RED);
            lightStatusTextView.setText(R.string.notificationLightStatusDisabled);
        }

        updateCheckBox();
        updateTimePickers();

    }

    private void updateCheckBox() {
        CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enableSchedulerCheckBox);
        enableCheckBox.setChecked(ImpulseControlApplication.isScheduleEnabled());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        updateDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scheduler, menu);
        return true;
    }

}
