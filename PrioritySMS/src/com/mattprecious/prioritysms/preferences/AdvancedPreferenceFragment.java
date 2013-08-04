package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import com.mattprecious.prioritysms.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AdvancedPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.advanced_preferences);

        EditTextPreference callLogDelayPreference = (EditTextPreference)
                findPreference(getString(R.string.pref_key_advanced_log_delay));
        callLogDelayPreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
    }
}