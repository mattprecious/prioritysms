/*
 * Copyright 2012 Matthew Precious
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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
