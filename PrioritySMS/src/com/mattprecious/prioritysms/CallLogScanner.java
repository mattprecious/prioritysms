package com.mattprecious.prioritysms;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

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

                phoneNumber = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
            }
        } finally {
            c.close();
        }

        // TODO: create a helper for this and the SMS receiver

        String contactLookupKey = settings.getString("call_contact", "");

        boolean contactMatch = false;

        // if we're filtering by contact, and
        // if we're alarming on phone call,
        // look up the contact id of our filtered contact, and
        // look up the contact id of the caller, and
        // check if they're the same ID
        if (phoneNumber != null && !contactLookupKey.equals("")) {
            Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactLookupKey);

            columns = new String[] { Contacts._ID };
            c = getContentResolver().query(contactUri, columns, null, null, null);

            if (c.moveToFirst()) {
                String contactId = c.getString(c.getColumnIndex(Contacts._ID));

                Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(phoneNumber));

                String[] columns2 = new String[] { PhoneLookup._ID };
                Cursor c2 = getContentResolver().query(phoneUri, columns2, null, null, null);

                if (c2.moveToFirst()) {
                    String thisContactId = c2.getString(c.getColumnIndex(PhoneLookup._ID));

                    contactMatch = thisContactId.equals(contactId);
                }

                c2.close();
            }

            c.close();
        } else {
            contactMatch = false;
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
