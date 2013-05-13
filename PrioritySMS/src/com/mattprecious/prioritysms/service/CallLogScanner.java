
package com.mattprecious.prioritysms.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;

import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.util.Intents;

import java.util.List;

public class CallLogScanner extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        String number = null;

        String[] columns = {
                CallLog.Calls.TYPE,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER
        };

        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, columns, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        try {
            if (c.moveToFirst()) {
                if (CallLog.Calls.MISSED_TYPE != c.getInt(c.getColumnIndex(CallLog.Calls.TYPE))) {
                    return;
                }

                // TODO: make sure this entry is recent and isn't from a
                // previous call.
                // Problem is that the date logged is the time the ringing was
                // initiated and not when the call was missed.

                number = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            }
        } finally {
            c.close();
        }

        if (number != null) {
            List<PhoneProfile> profiles = new DbAdapter(this).getEnabledPhoneProfiles();
            for (PhoneProfile profile : profiles) {
                if (profile.callMatches(this, number)) {
                    Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
                    alarmIntent.putExtra(Intents.EXTRA_PROFILE, profile);
                    alarmIntent.putExtra(Intents.EXTRA_NUMBER, number);

                    sendBroadcast(alarmIntent);
                }
            }
        }

        stopSelf();
    }
}
