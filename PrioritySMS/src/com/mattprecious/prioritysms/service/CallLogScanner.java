
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

package com.mattprecious.prioritysms.service;

import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.util.Intents;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;

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
