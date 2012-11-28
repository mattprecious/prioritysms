/*
 * Copyright 2011 Matthew Precious
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

import com.mattprecious.prioritysms.util.ContactHelper;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String sender = msgs[i].getOriginatingAddress();
                String message = msgs[i].getMessageBody();

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                boolean enabled         = settings.getBoolean("enabled", false);
                boolean filterKeyword   = settings.getBoolean("filter_keyword", false);
                boolean filterContact   = settings.getBoolean("filter_contact", false);

                String keyword          = settings.getString("keyword", "");
                String[] keywordArr     = keyword.split(",");
                String contactLookupKey = settings.getString("sms_contact", "");
                
                // return if we aren't filtering by anything
                if (!filterKeyword && !filterContact) {
                    return;
                }
                
                boolean keywordCondition = false;
                boolean contactCondition = false;

                // if we're filtering by keyword,
                // check if the keyword is set, and
                // check if the message contains the keyword
                if (filterKeyword) {
                    for (String key : keywordArr) {
                        if (!key.equals("") && (message.toLowerCase().indexOf(key.toLowerCase()) != -1)) {
                            keywordCondition = true;
                            break;
                        }
                    }
                } else {
                    keywordCondition = true;
                }
                
                if (!keywordCondition) {
                    return;
                }

                // if we're filtering by contact,
                // look up the contact id of our filtered contact, and
                // look up the contact id of the sender, and
                // check if they're the same ID
                if (filterContact && !contactLookupKey.equals("")) {
                	String contactId = ContactHelper.getContactIdByLookupKey(context, contactLookupKey);
                	String incomingContactId = ContactHelper.getContactIdByNumber(context, sender);
                	
                	contactCondition = contactId != null && contactId.equals(incomingContactId);
                } else {
                    contactCondition = true;
                }

                // if we're enabled and all of our conditions are met, open the
                // notification
                if (enabled && keywordCondition && contactCondition) {
                    Intent newIntent = new Intent(context, Notification.class);

                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    newIntent.putExtra("sender", sender);
                    newIntent.putExtra("message", message);

                    context.startActivity(newIntent);

                }
            }
        }

    }
}
