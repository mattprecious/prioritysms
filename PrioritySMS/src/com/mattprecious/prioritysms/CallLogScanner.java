package com.mattprecious.prioritysms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;

import com.mattprecious.prioritysms.util.ContactHelper;

public class CallLogScanner extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        String phoneNumber = null;

        String[] columns = { CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER };
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, columns, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        try {
            if (c.moveToFirst()) {
                if (CallLog.Calls.MISSED_TYPE != c.getInt(c.getColumnIndex(CallLog.Calls.TYPE))) {
                    return;
                }

                // TODO: make sure this entry is recent and isn't from a previous call

                phoneNumber = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            }
        } finally {
            c.close();
        }

        String contactLookupKey = settings.getString("call_contact", "");

        boolean contactMatch = false;

        // look up the contact id of our filtered contact, and
        // look up the contact id of the caller, and
        // check if they're the same ID
        if (phoneNumber != null && !contactLookupKey.equals("")) {
            String contactId = ContactHelper.getContactIdByLookupKey(this, contactLookupKey);
            String incomingContactId = ContactHelper.getContactIdByNumber(this, phoneNumber);

            contactMatch = contactId != null && contactId.equals(incomingContactId);
        }

        if (contactMatch) {
            Intent newIntent = new Intent(this, Notification.class);

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("sender", phoneNumber);
            newIntent.putExtra("is_call", true);

            startActivity(newIntent);
        }

        stopSelf();
    }
}
