package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.util.Helpers;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

public class SettingsActivity extends SherlockPreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String ENCODING = "UTF-8";

    public static final String PREFS_ALARM =
            "com.mattprecious.prioritysms.preferences.PREFS_ALARM";
    public static final String PREFS_ABOUT =
            "com.mattprecious.prioritysms.preferences.PREFS_ABOUT";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String action = getIntent().getAction();
        if (PREFS_ALARM.equals(action)) {
            addPreferencesFromResource(R.xml.alarm_preferences);

            ListPreference timeoutPreference =
                    (ListPreference) findPreference(getString(R.string.pref_key_alarm_timeout));
            updateTimeoutSummary(timeoutPreference, timeoutPreference.getValue());
            timeoutPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            updateTimeoutSummary(preference, (String) newValue);
                            return true;
                        }
                    });
        } else if (PREFS_ABOUT.equals(action)) {
            addPreferencesFromResource(R.xml.about_preferences);
            findPreference(getString(R.string.pref_key_about_version))
                    .setSummary(getAppVersion(this));
            findPreference(getString(R.string.pref_key_about_change_log))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            buildChangeLogDialog(SettingsActivity.this).show();
                            return false;
                        }
                    });
            findPreference(getString(R.string.pref_key_about_attributions))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            buildAttributionsDialog(SettingsActivity.this).show();
                            return false;
                        }
                    });
            findPreference(getString(R.string.pref_key_about_translate))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Helpers.openTranslatePage(SettingsActivity.this);
                            return false;
                        }
                    }
            );
            findPreference(getString(R.string.pref_key_about_feedback))
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Helpers.openSupportPage(SettingsActivity.this);
                            return false;
                        }
                    });
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_headers_legacy);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
        updateHeaderList(target);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateHeaderList(List<Header> target) {
//        int i = 0;
//        while (i < target.size()) {
//            Header header = target.get(i);
//            if (i < target.size() && target.get(i) == header) {
//                i++;
//            }
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void updateTimeoutSummary(Preference preference, String delay) {
        int i = Integer.parseInt(delay);
        if (i == -1) {
            preference.setSummary(R.string.alarm_timeout_summary_never);
        } else {
            preference.setSummary(
                    preference.getContext().getString(R.string.alarm_timeout_summary_other, i));
        }
    }

    public static String getAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);

            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return null;
    }

    public static Dialog buildChangeLogDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.change_log_title);

        InputStream changelogStream = context.getResources().openRawResource(R.raw.changelog);
        Scanner s = new Scanner(changelogStream, ENCODING).useDelimiter("\\A");
        String changelogHtml = s.hasNext() ? s.next() : "";

        try {
            changelogHtml = URLEncoder.encode(changelogHtml, ENCODING).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "failed to encode change log html", e);
        }

        WebView webView = new WebView(context);
        webView.loadData(changelogHtml, "text/html", ENCODING);
        builder.setView(webView);

        builder.setNeutralButton(R.string.change_log_close, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public static Dialog buildAttributionsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.about_attributions, null);

        // for some reason when you replace the view on a legacy dialog it wipes
        // the background colour...
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            rootView.setBackgroundColor(context.getResources().getColor(
                    android.R.color.background_light));
        }

        TextView attributionsView = (TextView) rootView.findViewById(R.id.attributions);
        attributionsView.setText(Html.fromHtml(context.getString(R.string.attributions)));
        attributionsView.setMovementMethod(new LinkMovementMethod());

        builder.setTitle(R.string.attributions_title);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.attributions_close, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}