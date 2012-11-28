package com.mattprecious.prioritysms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // check if this is an incoming call
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            return;
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        boolean enabled = settings.getBoolean("enabled", false);
        boolean onCall = settings.getBoolean("on_call", false);
        if (!enabled || !onCall) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getService(context, 0, new Intent(context,
                CallLogScanner.class), 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager
                .set(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis()
                                + Long.valueOf(settings.getString("call_log_delay", "2000")),
                        pendingIntent);
    }
}
