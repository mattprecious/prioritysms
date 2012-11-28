package com.mattprecious.prioritysms.util;

import com.mattprecious.prioritysms.R;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;

public class ContactHelper {
    public static String getLookupKeyByUri(Context context, Uri contactUri) {
        String[] columns = new String[] { Contacts.LOOKUP_KEY };
        Cursor c = context.getContentResolver().query(contactUri, columns, null, null, null);

        String lookupKey = "";
        if (c.moveToFirst()) {
            lookupKey = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY));
        }

        c.close();

        return lookupKey;
    }

    public static String getNameByLookupKey(Context context, String lookupKey) {
        String name = context.getString(R.string.na);
        if (!lookupKey.equals("")) {
            Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);

            String[] columns = new String[] { Contacts.DISPLAY_NAME };
            Cursor c = context.getContentResolver().query(lookupUri, columns, null, null, null);

            if (c != null && c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
            }

            c.close();
        }

        return name;
    }

    public static String getNameByNumber(Context context, String number) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] columns = new String[] { PhoneLookup.DISPLAY_NAME };
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

        String name = number;
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }

        c.close();

        return name;
    }

    public static String getContactIdByLookupKey(Context context, String lookupKey) {
        Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);

        String[] columns = new String[] { Contacts._ID };
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

        String contactId = null;
        if (c.moveToFirst()) {
            contactId = c.getString(c.getColumnIndex(Contacts._ID));
        }

        c.close();

        return contactId;
    }

    public static String getContactIdByNumber(Context context, String number) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] columns = new String[] { PhoneLookup._ID };
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

        String contactId = null;
        if (c.moveToFirst()) {
            contactId = c.getString(c.getColumnIndex(PhoneLookup._ID));
        }

        c.close();

        return contactId;
    }
}
