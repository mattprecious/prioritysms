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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
    private Preference         contactPreference;
    private RingtonePreference alarmPreference;
    
    private final int REQUEST_CODE_CONTACT_PICKER = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
        
        settings = ((PreferenceScreen) findPreference("preferences")).getSharedPreferences();
        
        enabledPreference       = (CheckBoxPreference) findPreference("enabled");
        filterKeywordPreference = (CheckBoxPreference) findPreference("filter_keyword");
        keywordPreference       = (EditTextPreference) findPreference("keyword");
        filterContactPreference = (CheckBoxPreference) findPreference("filter_contact");
        contactPreference       = (Preference)         findPreference("contact");
        alarmPreference         = (RingtonePreference) findPreference("alarm");
        
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
        
        contactPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, REQUEST_CODE_CONTACT_PICKER);
                return false;
            }
        });
        
        updateKeyword();
        updateContact();
        updateAlarm();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CONTACT_PICKER:
                    Uri contactUri = data.getData();
                    
                    String[] columns = new String[]{Contacts.LOOKUP_KEY};
                    Cursor c = getContentResolver().query(contactUri, columns, null, null, null);
                    
                    String lookupKey = "";
                    if (c.moveToFirst()) {
                        lookupKey = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY));
                    }
                    
                    c.close();

                    Editor editor = settings.edit();
                    editor.putString("contact", lookupKey);
                    editor.commit();
                    
                    updateContact();
                    
                    return;
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data);
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
    private void updateContact() {
        String lookupKey = settings.getString("contact", "");
        
        String name = "N/A";
        if (!lookupKey.equals("")) {
            Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
            
            String[] columns = new String[]{Contacts.DISPLAY_NAME};
            Cursor c = getContentResolver().query(lookupUri, columns, null, null, null);
            
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
            }
            
            c.close();
        }
        
        findPreference("contact").setSummary(name);
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
        alarmPreference.setSummary(ringtone.getTitle(getApplicationContext()));
    }
}