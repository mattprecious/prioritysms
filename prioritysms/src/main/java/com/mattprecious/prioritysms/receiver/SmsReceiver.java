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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.Intents;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

  @Override public void onReceive(@NonNull Context context, @NonNull Intent intent) {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    boolean enabled = settings.getBoolean(context.getString(R.string.pref_key_enabled), false);
    if (!enabled) {
      return;
    }

    Bundle bundle = intent.getExtras();
    SmsMessage[] msgs;
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      if (pdus == null) return;

      msgs = new SmsMessage[pdus.length];
      for (int i = 0; i < msgs.length; i++) {
        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        String sender = msgs[i].getOriginatingAddress();
        String message = msgs[i].getMessageBody();

        List<SmsProfile> profiles = new DbAdapter(context).getEnabledSmsProfiles();
        for (SmsProfile profile : profiles) {
          if (profile.messageMatches(context, sender, message)) {
            Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
            alarmIntent.putExtra(Intents.EXTRA_PROFILE, profile);
            alarmIntent.putExtra(Intents.EXTRA_NUMBER, sender);
            alarmIntent.putExtra(Intents.EXTRA_MESSAGE, message);

            context.sendBroadcast(alarmIntent);
            return;
          }
        }
      }
    }
  }
}
