/*
 * Copyright 2013 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.service.CallLogScanner;

public class PhoneStateReceiver extends BroadcastReceiver {

  @Override public void onReceive(@NonNull Context context, @NonNull Intent intent) {
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

    long delay = Long.valueOf(
        settings.getString(context.getString(R.string.pref_key_advanced_log_delay), "2000"));
    long wakeupTime = System.currentTimeMillis() + delay;

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC_WAKEUP, wakeupTime, pendingScanner);
  }
}
