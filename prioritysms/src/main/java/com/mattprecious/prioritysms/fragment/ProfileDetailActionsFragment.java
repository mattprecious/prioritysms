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

package com.mattprecious.prioritysms.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.BaseDetailFragment;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.ValidationResponse;
import com.mattprecious.prioritysms.model.ActionType;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;

public class ProfileDetailActionsFragment extends BaseDetailFragment {

  public static final String EXTRA_PROFILE = "profile";

  public static ProfileDetailActionsFragment create(BaseProfile profile) {
    Bundle args = new Bundle();
    args.putParcelable(EXTRA_PROFILE, profile);

    ProfileDetailActionsFragment fragment = new ProfileDetailActionsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private static final int REQUEST_CODE_RINGTONE_PICKER = 1;

  @Bind(R.id.action_type) Spinner actionTypeSpinner;
  @Bind(R.id.action_sound) Button soundButton;
  @Bind(R.id.action_override_silent) CheckBox overrideSilentCheckBox;
  @Bind(R.id.action_vibrate) CheckBox vibrateCheckBox;

  private BaseProfile profile;
  private boolean spinnerReady;
  private Uri ringtoneBackup;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args == null || !args.containsKey(EXTRA_PROFILE)) {
      throw new IllegalArgumentException(String.format("must pass %s as an extra", EXTRA_PROFILE));
    }

    profile = args.getParcelable(EXTRA_PROFILE);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.profile_detail_actions, container, false);
    ButterKnife.bind(this, rootView);

    actionTypeSpinner.setAdapter(new ActionTypeAdapter(getActivity()));
    actionTypeSpinner.setOnItemSelectedListener(mActionTypeSelectedListener);
    actionTypeSpinner.setSelection(profile.getActionType().ordinal());

    updateRingtone(profile.getRingtone());
    soundButton.setOnClickListener(new OnClickListener() {

      @Override public void onClick(@NonNull View v) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
            getSelectedActionType() == ActionType.ALARM ? RingtoneManager.TYPE_ALARM
                : RingtoneManager.TYPE_NOTIFICATION
        );
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) v.getTag());

        startActivityForResult(intent, REQUEST_CODE_RINGTONE_PICKER);
      }
    });

    overrideSilentCheckBox.setChecked(profile.isOverrideSilent());
    vibrateCheckBox.setChecked(profile.isVibrate());

    return rootView;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CODE_RINGTONE_PICKER:
        if (resultCode == Activity.RESULT_OK) {
          Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
          updateRingtone(ringtoneUri);
        }

        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }
  }

  @Override public void updateProfile(BaseProfile profile) {
    profile.setRingtone((Uri) soundButton.getTag());
    profile.setOverrideSilent(overrideSilentCheckBox.isChecked());
    profile.setVibrate(vibrateCheckBox.isChecked());

    if (profile instanceof SmsProfile) {
      profile.setActionType(getSelectedActionType());
    }
  }

  @Override public ValidationResponse validate() {
    return new ValidationResponse(true);
  }

  private ActionType getSelectedActionType() {
    return ActionType.values()[actionTypeSpinner.getSelectedItemPosition()];
  }

  private void updateRingtone(Uri ringtoneUri) {
    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
    String ringtoneName =
        (ringtoneUri == null || ringtone == null) ? getString(R.string.actions_ringtone_silent)
            : ringtone.getTitle(getActivity());
    soundButton.setText(getString(R.string.actions_ringtone, ringtoneName));
    soundButton.setTag(ringtoneUri);
  }

  private AdapterView.OnItemSelectedListener mActionTypeSelectedListener =
      new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          if (spinnerReady) {
            Uri tempRingtone = (Uri) soundButton.getTag();
            updateRingtone(ringtoneBackup);
            ringtoneBackup = tempRingtone;
          } else {
            spinnerReady = true;
          }

          ActionType actionType = ActionType.values()[position];
          overrideSilentCheckBox.setVisibility(
              actionType == ActionType.ALARM ? View.GONE : View.VISIBLE);
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {

        }
      };

  private class ActionTypeAdapter extends ArrayAdapter<String> {
    final LayoutInflater inflater;
    final String[] items;

    private ActionTypeAdapter(Context context) {
      super(context, android.R.layout.simple_spinner_item);

      inflater = LayoutInflater.from(context);
      items = context.getResources().getStringArray(R.array.profile_action_names);
    }

    @Override public int getCount() {
      return items.length;
    }

    @Override public String getItem(int position) {
      return items[position];
    }

    @Override public int getPosition(String item) {
      for (int i = 0; i < items.length; i++) {
        if (item.equals(items[i])) {
          return i;
        }
      }

      return -1;
    }

    @Override public long getItemId(int position) {
      if (position < getCount() && position >= 0) {
        return position;
      } else {
        throw new IndexOutOfBoundsException();
      }
    }

    @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.profile_detail_header_spinner_item, parent, false);
      }

      ((TextView) convertView).setText(getItem(position));

      return convertView;
    }

    @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView =
            inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
      }

      ((TextView) convertView).setText(getItem(position));

      return convertView;
    }
  }
}
