
package com.mattprecious.prioritysms.receiver;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.service.CallLogScanner;

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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = settings.getBoolean(context.getString(R.string.pref_key_enabled), false);
        if (!enabled) {
            return;
        }

        // check if this is an incoming call
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            return;
        }

        Intent scannerIntent = new Intent(context, CallLogScanner.class);
        PendingIntent pendingScanner = PendingIntent.getService(context, 0, scannerIntent, 0);

        long delay = Long.valueOf(settings.getString(
                context.getString(R.string.pref_key_log_delay), "2000"));
        long wakeupTime = System.currentTimeMillis() + delay;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, wakeupTime, pendingScanner);
    }
}
