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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;

public class PrioritySMS extends PreferenceActivity {
    
    private OnSharedPreferenceChangeListener prefListener;
    private SharedPreferences settings;
    
    private CheckBoxPreference enabledPreference;
    private EditTextPreference keywordPreference;
    private RingtonePreference alarmPreference;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
        
        settings = ((PreferenceScreen) findPreference("preferences")).getSharedPreferences();
        
        enabledPreference = (CheckBoxPreference) findPreference("enabled");
        keywordPreference = (EditTextPreference) findPreference("keyword");
        alarmPreference   = (RingtonePreference) findPreference("alarm");
        
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
        
        updateKeyword();
        updateAlarm();
    }
    
    private void updateKeyword() {
        String keyword = settings.getString("keyword", "");
        keyword = (keyword.equals("")) ? "N/A" : keyword;
        findPreference("keyword").setSummary(keyword);
    }
    
    private void updateAlarm() {
        String alarm = settings.getString("alarm", null);
        Uri uri = (alarm == null) ? 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                Uri.parse(alarm);
                
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        alarmPreference.setSummary(ringtone.getTitle(getApplicationContext()));
    }
}