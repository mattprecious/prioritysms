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
