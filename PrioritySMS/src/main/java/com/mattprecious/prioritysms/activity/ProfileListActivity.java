/*
 * Copyright 2013 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.mattprecious.prioritysms.BuildConfig;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.billing.IabHelper;
import com.mattprecious.prioritysms.billing.IabResult;
import com.mattprecious.prioritysms.billing.Inventory;
import com.mattprecious.prioritysms.billing.Purchase;
import com.mattprecious.prioritysms.devtools.TriggerAlarmPhoneDialogFragment;
import com.mattprecious.prioritysms.devtools.TriggerAlarmSmsDialogFragment;
import com.mattprecious.prioritysms.fragment.ChangeLogDialogFragment;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment;
import com.mattprecious.prioritysms.fragment.ProfileLimitDialogFragment;
import com.mattprecious.prioritysms.fragment.ProfileListFragment;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.preferences.AboutPreferenceFragment;
import com.mattprecious.prioritysms.preferences.SettingsActivity;
import com.mattprecious.prioritysms.util.Helpers;
import com.mattprecious.prioritysms.util.Strings;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.jraf.android.backport.switchwidget.Switch;

public class ProfileListActivity extends BaseActivity
    implements ProfileListFragment.Callbacks, ProfileDetailFragment.Callbacks,
    IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener,
    ProfileLimitDialogFragment.Callbacks {
  private static final String TAG = ProfileListActivity.class.getSimpleName();
  private static final String STATE_DETAIL_FRAGMENT = "detail_fragment";

  private static final String KEY_CHANGE_LOG_VERSION = "change_log_version";
  private static final String KEY_IS_PRO = "is_pro";

  private static final int REQUEST_ID_PROFILE_EDIT = 1;
  private static final int REQUEST_ID_GO_PRO = 2;

  private static final Style CROUTON_STYLE_DELETE =
      new Style.Builder(Style.INFO).setPaddingDimensionResId(R.dimen.crouton_delete_padding)
          .build();

  private boolean twoPane;
  private boolean pro;

  private SharedPreferences preferences;
  private IabHelper iabHelper;
  private boolean iabSetupDone;

  private Switch actionBarSwitch;
  private ProfileListFragment listFragment;
  private Fragment detailFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_profile_list);
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    checkUpdated();

    pro = preferences.getBoolean(KEY_IS_PRO, false);

    iabHelper = new IabHelper(this, getString(R.string.iap_key));
    iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          Log.d(TAG, "Problem setting up In-app Billing: " + result);
          return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (iabHelper == null) {
          return;
        }

        iabSetupDone = true;
        iabHelper.queryInventoryAsync(ProfileListActivity.this);
      }
    });

    configureActionBar();

    listFragment =
        (ProfileListFragment) getSupportFragmentManager().findFragmentById(R.id.profile_list);

    if (savedInstanceState != null) {
      detailFragment =
          getSupportFragmentManager().getFragment(savedInstanceState, STATE_DETAIL_FRAGMENT);
    }

    if (findViewById(R.id.profile_detail_container) != null) {
      twoPane = true;

      listFragment.setActivateOnItemClick(true);

      if (detailFragment != null) {
        setHasOptionsMenu(false);
      }
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    Crouton.clearCroutonsForActivity(this);

    if (iabHelper != null) {
      iabHelper.dispose();
      iabHelper = null;
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (detailFragment != null) {
      getSupportFragmentManager().putFragment(outState, STATE_DETAIL_FRAGMENT, detailFragment);
    }
  }

  @Override public void onBackPressed() {
    if (detailFragment == null) {
      super.onBackPressed();
    } else {
      onDiscard();
    }
  }

  @Override public void onItemSelected(BaseProfile profile) {
    if (twoPane) {
      boolean wasEditing = detailFragment != null;

      detailFragment = ProfileDetailFragment.create(profile);
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.profile_detail_container, detailFragment)
          .commit();

      if (wasEditing) {
        showDiscardCrouton();
      } else {
        setHasOptionsMenu(false);
      }
    } else {
      Intent detailIntent = new Intent(this, ProfileDetailActivity.class);
      detailIntent.putExtra(ProfileDetailFragment.EXTRA_PROFILE, profile);
      startActivityForResult(detailIntent, REQUEST_ID_PROFILE_EDIT);
    }

    Crouton.cancelAllCroutons();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.menu_profile_list_activity, menu);

    if (BuildConfig.DEBUG) {
      menu.findItem(R.id.menu_dev_tools).setVisible(true);
    }

    return true;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.menu_pro).setVisible(!pro);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onDiscard();
        return true;
      case R.id.menu_dev_sms:
        new TriggerAlarmSmsDialogFragment().show(getSupportFragmentManager(), null);
        return true;
      case R.id.menu_dev_phone:
        new TriggerAlarmPhoneDialogFragment().show(getSupportFragmentManager(), null);
        return true;
      case R.id.menu_preferences:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.menu_pro:
        onGoProClick();
        return true;
      case R.id.menu_feedback:
        Helpers.openSupportPage(this);
        return true;
      case R.id.menu_about:
        startActivity(buildAboutIntent());
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (iabHelper != null && iabHelper.handleActivityResult(requestCode, resultCode, data)) {
      return;
    }

    switch (requestCode) {
      case REQUEST_ID_PROFILE_EDIT:
        switch (resultCode) {
          case RESULT_OK:
            showSaveCrouton();
            break;
          case RESULT_CANCELED:
            showDiscardCrouton();
            break;
          case ProfileDetailActivity.RESULT_DELETED:
            showDeleteCrouton(
                (BaseProfile) data.getParcelableExtra(ProfileDetailFragment.EXTRA_PROFILE));
            break;
          default:
            break;
        }
        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }
  }

  @Override public void onQueryInventoryFinished(IabResult result, Inventory inv) {
    if (iabHelper == null) {
      return;
    }

    if (result.isSuccess()) {
      setPro(inv.hasPurchase(getString(R.string.iap_sku_pro)));
    }
  }

  @Override public void onIabPurchaseFinished(IabResult result, Purchase info) {
    if (iabHelper == null) {
      return;
    }

    if (result.isFailure()) {
      if (result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED) {
        Crashlytics.log(Log.ERROR, TAG, "Error purchasing: " + result);
        showCrouton(R.string.pro_purchase_failed, Style.ALERT);
      }

      return;
    }

    setPro(true);
    showCrouton(R.string.pro_purchase_success, Style.CONFIRM);
  }

  private void setPro(boolean isPro) {
    pro = isPro;
    invalidateOptionsMenu();

    // not sure if I need to do this... play seems to cache this
    preferences.edit().putBoolean(KEY_IS_PRO, isPro).commit();
  }

  private void configureActionBar() {
    actionBarSwitch = new Switch(this, null, R.attr.switchStyleAb);
    actionBarSwitch.setChecked(preferences.getBoolean(getString(R.string.pref_key_enabled), false));
    actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        preferences.edit().putBoolean(getString(R.string.pref_key_enabled), isChecked).commit();
      }
    });

    final int padding = getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
    actionBarSwitch.setPadding(0, 0, padding, 0);
    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
        ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(actionBarSwitch,
        new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
  }

  private void setHasOptionsMenu(boolean hasMenu) {
    actionBarSwitch.setVisibility(hasMenu ? View.VISIBLE : View.GONE);
    listFragment.setHasOptionsMenu(hasMenu);
  }

  private void refreshList() {
    listFragment.refreshList();
  }

  private void removeDetailFragment() {
    if (detailFragment != null) {
      getSupportFragmentManager().beginTransaction().remove(detailFragment).commit();
      detailFragment = null;
      setHasOptionsMenu(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    listFragment.clearActivated();

    hideKeyboard();
  }

  private void showSaveCrouton() {
    showCrouton(R.string.crouton_profile_saved, Style.CONFIRM);
  }

  private void showDiscardCrouton() {
    showCrouton(R.string.crouton_profile_discarded, Style.INFO);
  }

  private void showDeleteCrouton(final BaseProfile profile) {
    Crouton crouton = makeCrouton(R.string.crouton_profile_deleted, CROUTON_STYLE_DELETE);
    crouton.setConfiguration(
        new Configuration.Builder().setDuration(Configuration.DURATION_LONG).build());
    crouton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Crouton.clearCroutonsForActivity(ProfileListActivity.this);
        profile.undoDelete(getApplicationContext());
        refreshList();
      }
    });

    crouton.show();
  }

  private void showCrouton(int messageResId, Style style) {
    makeCrouton(messageResId, style).show();
  }

  private Crouton makeCrouton(int messageResId, Style style) {
    return Crouton.makeText(this, messageResId, style,
        twoPane ? R.id.profile_detail_container : android.R.id.content);
  }

  @Override public void onNewProfile(BaseProfile profile) {
    onItemSelected(profile);
  }

  @Override public boolean isPro() {
    return pro;
  }

  @Override public void onGoProClick() {
    if (iabSetupDone) {
      iabHelper.launchPurchaseFlow(this, getString(R.string.iap_sku_pro), REQUEST_ID_GO_PRO, this);
    } else {
      Crouton.showText(this, R.string.iab_not_ready, Style.ALERT);
    }
  }

  @Override public void onNameUpdated(String name) {
  }

  @Override public void onDiscard() {
    removeDetailFragment();
    showDiscardCrouton();
  }

  @Override public void onDelete(BaseProfile profile) {
    removeDetailFragment();
    refreshList();
    showDeleteCrouton(profile);
  }

  @Override public void onSave() {
    removeDetailFragment();
    refreshList();
    showSaveCrouton();
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
  }

  private Intent buildAboutIntent() {
    Intent aboutIntent;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      aboutIntent = buildAboutIntentLegacy();
    } else {
      aboutIntent = buildAboutIntentFragments();
    }

    return aboutIntent;
  }

  private Intent buildAboutIntentLegacy() {
    Intent aboutIntent = new Intent(this, SettingsActivity.class);
    aboutIntent.setAction(SettingsActivity.PREFS_ABOUT);

    return aboutIntent;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private Intent buildAboutIntentFragments() {
    Intent aboutIntent = new Intent(this, SettingsActivity.class);
    aboutIntent.putExtra(SherlockPreferenceActivity.EXTRA_SHOW_FRAGMENT,
        AboutPreferenceFragment.class.getName());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      setAboutIntentTitleICS(aboutIntent);
    }

    return aboutIntent;
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setAboutIntentTitleICS(Intent intent) {
    intent.putExtra(SherlockPreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE,
        R.string.preference_header_about);
  }

  private void checkUpdated() {
    PackageManager packageManager = getPackageManager();

    try {
      PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

      // the old code stored this in the global settings, so fall back to that
      int lastVersion = getPreferences(MODE_PRIVATE).getInt(KEY_CHANGE_LOG_VERSION,
          preferences.getInt("version_code", 0));

      // set some defaults
      if (lastVersion == 0) {
        preferences.edit()
            .putBoolean(getString(R.string.pref_key_enabled), true)
            .putBoolean(getString(R.string.pref_key_general_analytics), true)
            .commit();
      }

      int currentVersion = packageInfo.versionCode;
      if (lastVersion < currentVersion) {
        doUpdate(lastVersion, currentVersion);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          showChangeLog();
        } else {
          showChangeLogLegacy();
        }

        getPreferences(MODE_PRIVATE).edit()
            .putInt(KEY_CHANGE_LOG_VERSION, packageInfo.versionCode)
            .commit();
      }
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Failed to show change log", e);
    }
  }

  // TODO: move this to another file
  // TODO: pop loading dialog
  private void doUpdate(int from, int to) {
    switch (from) {
      case 1:
      case 2:
        // too old, don't worry
        break;
      case 3:
        // move 'contact' preference to 'sms_contact'
        preferences.edit()
            .putString("sms_contact", preferences.getString("contact", null))
            .remove("contact")
            .commit();

      case 4:
        // enabled defaulted to false previously
        preferences.edit()
            .putBoolean(getString(R.string.pref_key_enabled),
                preferences.getBoolean("enabled", false))
            .commit();

        SmsProfile smsProfile = new SmsProfile();
        smsProfile.setName("SMS Profile");

        PhoneProfile phoneProfile = new PhoneProfile();
        phoneProfile.setName("Phone Profile");

        String ringtone = preferences.getString("alarm", null);
        Uri ringtoneUri =
            ringtone == null ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                : Uri.parse(ringtone);
        smsProfile.setRingtone(ringtoneUri);
        phoneProfile.setRingtone(ringtoneUri);

        boolean vibrate = preferences.getBoolean("vibrate", false);
        smsProfile.setVibrate(vibrate);
        phoneProfile.setVibrate(vibrate);

        // import the SMS settings
        boolean smsWorthSaving = false;

        String smsLookup = preferences.getString("sms_contact", null);
        if (preferences.getBoolean("filter_contact", false) && !Strings.isBlank(smsLookup)) {
          smsProfile.addContact(smsLookup);
          smsWorthSaving = true;
        }

        String keywords = preferences.getString("keyword", null);
        if (preferences.getBoolean("filter_keyword", false) && !Strings.isBlank(keywords)) {
          String[] keywordArr = keywords.split(",");
          smsProfile.setKeywords(new LinkedHashSet<String>(Arrays.asList(keywordArr)));
          smsWorthSaving = true;
        }

        if (smsWorthSaving) {
          smsProfile.save(this);
        }

        // import the missed call settings
        phoneProfile.setEnabled(preferences.getBoolean("on_call", false));

        boolean phoneWorthSaving = false;

        String phoneLookup = preferences.getString("call_contact", null);
        if (!Strings.isBlank(phoneLookup)) {
          phoneProfile.addContact(phoneLookup);
          phoneWorthSaving = true;
        }

        if (phoneWorthSaving) {
          phoneProfile.save(this);
        }

        // delete all those old preferences
        preferences.edit()
            .remove("filter_keyword")
            .remove("keyword")
            .remove("filter_contact")
            .remove("sms_contact")
            .remove("on_call")
            .remove("call_contact")
            .remove("alarm")
            .remove("override")
            .remove("vibrate")
            .commit();
    }
  }

  private void showChangeLogLegacy() {
    SettingsActivity.buildChangeLogDialog(this).show();
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void showChangeLog() {
    new ChangeLogDialogFragment().show(getFragmentManager(), null);
  }
}
