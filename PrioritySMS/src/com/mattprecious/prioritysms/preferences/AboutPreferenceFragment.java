package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.AttributionsDialogFragment;
import com.mattprecious.prioritysms.fragment.ChangeLogDialogFragment;
import com.mattprecious.prioritysms.util.Helpers;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AboutPreferenceFragment extends BasePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about_preferences);

        findPreference(getString(R.string.pref_key_about_version))
                .setSummary(SettingsActivity.getAppVersion(getActivity()));
        findPreference(getString(R.string.pref_key_about_change_log)).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DialogFragment changeLogDialog = new ChangeLogDialogFragment();
                        changeLogDialog.show(getFragmentManager(), null);

                        return false;
                    }
                });
        findPreference(getString(R.string.pref_key_about_attributions))
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DialogFragment licensesDialog = new AttributionsDialogFragment();
                        licensesDialog.show(getFragmentManager(), null);

                        return false;
                    }
                });
        findPreference(getString(R.string.pref_key_about_translate)).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Helpers.openTranslatePage(getActivity());
                        return false;
                    }
                });
        findPreference(getString(R.string.pref_key_about_feedback)).setOnPreferenceClickListener(
                new OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Helpers.openSupportPage(getActivity());
                        return false;
                    }
                });
    }
}
