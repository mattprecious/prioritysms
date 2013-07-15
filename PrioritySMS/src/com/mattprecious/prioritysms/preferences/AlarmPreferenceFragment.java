package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.mattprecious.prioritysms.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AlarmPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.alarm_preferences);

        ListPreference timeoutPreference =
                (ListPreference) findPreference(getString(R.string.pref_key_alarm_timeout));
        SettingsActivity.updateTimeoutSummary(timeoutPreference, timeoutPreference.getValue());
        timeoutPreference.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        SettingsActivity.updateTimeoutSummary(preference, (String) newValue);
                        return true;
                    }
                });
    }
}
