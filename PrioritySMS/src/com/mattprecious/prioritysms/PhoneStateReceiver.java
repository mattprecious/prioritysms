package com.mattprecious.prioritysms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    Bundle bundle = intent.getExtras();
	    String phoneNumber= bundle.getString("incoming_number");

	    // check if this is an incoming call
	    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
	    if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
	    	return;
	    }

        // TODO: create a helper for this and the SMS receiver

	    SharedPreferences settings = context.getSharedPreferences(context.getPackageName() + "_preferences", 0);

        boolean enabled = settings.getBoolean("enabled", false);
        if (!enabled || phoneNumber == null) {
        	return;
        }

        boolean onCall = settings.getBoolean("on_call", false);
        String contactLookupKey = settings.getString("call_contact", "");

        boolean contactMatch = false;

        // if we're filtering by contact, and
        // if we're alarming on phone call,
        // look up the contact id of our filtered contact, and
        // look up the contact id of the caller, and
        // check if they're the same ID
        if (onCall && !contactLookupKey.equals("")) {
            Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, contactLookupKey);

            String[] columns = new String[] { Contacts._ID };
            Cursor c = context.getContentResolver().query(contactUri, columns, null, null, null);

            if (c.moveToFirst()) {
                String contactId = c.getString(c.getColumnIndex(Contacts._ID));
                
                Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                
                String[] columns2 = new String[] { PhoneLookup._ID };
                Cursor c2 = context.getContentResolver().query(phoneUri, columns2, null, null, null);
                
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
        	Intent newIntent = new Intent(context, Notification.class);

            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("sender", phoneNumber);
            newIntent.putExtra("is_call", true);

            context.startActivity(newIntent);
        }
	}

}
