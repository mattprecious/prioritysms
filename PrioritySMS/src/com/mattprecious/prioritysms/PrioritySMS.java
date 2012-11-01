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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.ContactsContract.Contacts;

public class PrioritySMS extends PreferenceActivity {
    
    private OnSharedPreferenceChangeListener prefListener;
    private SharedPreferences settings;
    
    private CheckBoxPreference enabledPreference;
    private CheckBoxPreference filterKeywordPreference;
    private EditTextPreference keywordPreference;
    private CheckBoxPreference filterContactPreference;
    private Preference         smsContactPreference;
    private CheckBoxPreference onCallPreference;
    private Preference         callContactPreference;
    private RingtonePreference alarmPreference;
    private Preference         translatePreference;
    
    private final int REQUEST_CODE_SMS_CONTACT_PICKER = 1;
    private final int REQUEST_CODE_CALL_CONTACT_PICKER = 2;
    
    private final int DIALOG_ID_CHANGE_LOG = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
        
        settings = ((PreferenceScreen) findPreference("preferences")).getSharedPreferences();
        
        enabledPreference       = (CheckBoxPreference) findPreference("enabled");
        filterKeywordPreference = (CheckBoxPreference) findPreference("filter_keyword");
        keywordPreference       = (EditTextPreference) findPreference("keyword");
        filterContactPreference = (CheckBoxPreference) findPreference("filter_contact");
        smsContactPreference    = (Preference)         findPreference("sms_contact");
        onCallPreference        = (CheckBoxPreference) findPreference("on_call");
        callContactPreference   = (Preference)         findPreference("call_contact");
        alarmPreference         = (RingtonePreference) findPreference("alarm");
        translatePreference     = (Preference)         findPreference("translate");
        
        // register a listener for changes
        prefListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("keyword")) {
                    updateKeyword();
                } else if (key.equals("alarm")) {
                    updateAlarm();
                }
            }
        };
        
        settings.registerOnSharedPreferenceChangeListener(prefListener);
        
        smsContactPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, REQUEST_CODE_SMS_CONTACT_PICKER);
                return false;
            }
        });

        callContactPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, REQUEST_CODE_CALL_CONTACT_PICKER);
                return false;
            }
        });
        
        
        translatePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://crowdin.net/project/priority-sms"));
                startActivity(intent);
                
                return true;
            }
        });
        
        // debug the change log
        //settings.edit().putInt("version_code", 0).commit();
        
        checkIfUpdated();
        
        updateKeyword();
        updateContact("sms_contact");
        updateContact("call_contact");
        updateAlarm();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	Editor editor = settings.edit();

            switch (requestCode) {
            	case REQUEST_CODE_SMS_CONTACT_PICKER:
                    editor.putString("sms_contact", contactLookup(data.getData()));
                    editor.commit();
                    
                    updateContact("sms_contact");
                    
                    return;
            	case REQUEST_CODE_CALL_CONTACT_PICKER:
                    editor.putString("call_contact", contactLookup(data.getData()));
                    editor.commit();
                    
                    updateContact("call_contact");
                    
                    return;
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
            case DIALOG_ID_CHANGE_LOG:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.whats_new)
                       .setIcon(android.R.drawable.ic_dialog_info)
                       .setMessage(R.string.change_log)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                           
                           public void onClick(DialogInterface dialog, int id) {
                               dialog.cancel();
                           }
                       })
                       ;
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }
    
    /**
     * Show the keyword under the preference title
     */
    private void updateKeyword() {
        String keyword = settings.getString("keyword", "");
        keyword = (keyword.equals("")) ? "N/A" : keyword;
        findPreference("keyword").setSummary(keyword);
    }
    
    /**
     * Show the contact name under the preference title
     */
    private void updateContact(String settingsKey) {
        String lookupKey = settings.getString(settingsKey, "");
        
        String name = "N/A";
        if (!lookupKey.equals("")) {
            Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
            
            String[] columns = new String[]{Contacts.DISPLAY_NAME};
            Cursor c = getContentResolver().query(lookupUri, columns, null, null, null);
            
            if (c != null && c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
            }
            
            c.close();
        }
        
        findPreference(settingsKey).setSummary(name);
    }
    
    private String contactLookup(Uri contactUri) {
        String[] columns = new String[]{Contacts.LOOKUP_KEY};
        Cursor c = getContentResolver().query(contactUri, columns, null, null, null);
        
        String lookupKey = "";
        if (c.moveToFirst()) {
            lookupKey = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY));
        }
        
        c.close();

        return lookupKey;
    }
    
    /**
     * Show the chosen alarm under the preference title
     */
    private void updateAlarm() {
        String alarm = settings.getString("alarm", null);
        Uri uri = (alarm == null) ? 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                Uri.parse(alarm);
                
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        
        if (ringtone != null) {
            alarmPreference.setSummary(ringtone.getTitle(getApplicationContext()));
        }
    }
    
    private void checkIfUpdated() {
        PackageManager packageManager = getPackageManager();
        
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            
            if (settings.getInt("version_code", 0) != packageInfo.versionCode) {
            	// do some housekeeping
            	Editor editor = settings.edit();
            	
            	// move 'contact' preference to 'sms_contact' (v3 -> v4+)
            	editor.putString("sms_contact", settings.getString("contact", null));
            	editor.remove("contact");

                editor.putInt("version_code", packageInfo.versionCode);
                editor.commit();
            	
                // show change log
                showDialog(DIALOG_ID_CHANGE_LOG);
            }
        } catch (NameNotFoundException e) {
            
        }
    }
}