package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.InputType;
import com.mattprecious.prioritysms.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AdvancedPreferenceFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.advanced_preferences);
    }
}
