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

package com.mattprecious.prioritysms.util;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import com.google.common.collect.Sets;
import com.mattprecious.prioritysms.R;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

public class ContactHelper {
    private static final String TAG = ContactHelper.class.getSimpleName();

    public static String getLookupKeyByUri(Context context, Uri contactUri) {
        String[] columns = new String[]{Contacts.LOOKUP_KEY};
        Cursor c = context.getContentResolver().query(contactUri, columns, null, null, null);

        String lookupKey = "";
        if (c.moveToFirst()) {
            lookupKey = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY));
        }

        c.close();

        return lookupKey;
    }

    public static String getNameByLookupKey(Context context, String lookupKey) {
        String name = context.getString(R.string.contact_name_not_found);
        if (!lookupKey.equals("")) {
            Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);

            String[] columns = new String[]{Contacts.DISPLAY_NAME};
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

        String[] columns = new String[]{PhoneLookup.DISPLAY_NAME};
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

        String[] columns = new String[]{Contacts._ID};
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

        String contactId = null;
        if (c.moveToFirst()) {
            contactId = c.getString(c.getColumnIndex(Contacts._ID));
        }

        c.close();

        return contactId;
    }

    public static Set<String> getLookupKeysByNumber(Context context, String number) {
        Set<String> ids = Sets.newHashSet();

        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] columns = new String[]{PhoneLookup.LOOKUP_KEY};
        Cursor c = context.getContentResolver().query(uri, columns, null, null, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            ids.add(c.getString(c.getColumnIndex(PhoneLookup.LOOKUP_KEY)));
            c.moveToNext();
        }

        c.close();

        return ids;
    }

    public static Bitmap getContactPhoto(Context context, String lookup) {
        String contactIdString = getContactIdByLookupKey(context, lookup);
        if (contactIdString == null) {
            return getDefaultContactPhoto(context);
        }

        long contactId = Long.parseLong(contactIdString);
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);

        InputStream stream = null;
        try {
            stream = context.getContentResolver().openInputStream(photoUri);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "could not find contact picture");
        }

        if (stream == null) {
            return getDefaultContactPhoto(context);
        }

        return BitmapFactory.decodeStream(stream);
    }

    private static Bitmap getDefaultContactPhoto(Context context) {
        return BitmapFactory.decodeResource(context.getResources(),
            R.drawable.ic_contact_picture);
    }
}
