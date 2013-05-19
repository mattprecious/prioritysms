
package com.mattprecious.prioritysms.receiver;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.Intents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = settings.getBoolean(context.getString(R.string.pref_key_enabled), false);
        if (!enabled) {
            return;
        }

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
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
