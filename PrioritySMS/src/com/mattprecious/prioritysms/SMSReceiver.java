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
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);     
                String sender = msgs[i].getOriginatingAddress();
                String message = msgs[i].getMessageBody();
                
                SharedPreferences settings = context.getSharedPreferences(context.getPackageName() + "_preferences", 0);
                boolean enabled = settings.getBoolean("enabled", false);
                String keyword = settings.getString("keyword", "");
                
                if (enabled && !keyword.equals("") && message.toLowerCase().indexOf(keyword.toLowerCase()) != -1) {
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
