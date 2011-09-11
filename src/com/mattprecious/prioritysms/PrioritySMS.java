package com.mattprecious.prioritysms;

import android.content.Intent;
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
        
        Intent newIntent = new Intent(this, Notification.class);
        
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.putExtra("sender", "2063956665");
        newIntent.putExtra("message", "Lorem ipsum");
        
        startActivity(newIntent);
    }
    
    private void updateKeyword() {
        String keyword = settings.getString("keyword", "");
        keyword = (keyword.equals("")) ? "N/A" : keyword;
        findPreference("keyword").setSummary(keyword);
    }
    
    private void updateAlarm() {
        String alarm = settings.getString("alarm", "");
        Uri uri = (alarm == null) ? 
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) :
                Uri.parse(alarm);
                
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        alarmPreference.setSummary(ringtone.getTitle(getApplicationContext()));
    }
}