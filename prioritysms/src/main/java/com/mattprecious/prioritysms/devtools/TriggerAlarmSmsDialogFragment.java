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

package com.mattprecious.prioritysms.devtools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.Intents;
import java.util.ArrayList;
import java.util.List;

public class TriggerAlarmSmsDialogFragment extends SherlockDialogFragment {

  @Bind(R.id.profile_spinner) Spinner profileSpinner;
  @Bind(R.id.number) EditText numberText;
  @Bind(R.id.message) EditText messageText;
  @Bind(R.id.go) Button goButton;

  private List<SmsProfile> smsProfileList;

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    View rootView = inflater.inflate(R.layout.dev_tools_trigger_alarm_sms, null);
    ButterKnife.bind(this, rootView);

    smsProfileList = new DbAdapter(getActivity()).getEnabledSmsProfiles();

    List<String> profileNames = new ArrayList<>(smsProfileList.size());
    for (BaseProfile profile : smsProfileList) {
      profileNames.add(profile.getName());
    }

    profileSpinner.setAdapter(
        new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
            profileNames)
    );

    goButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(@NonNull View v) {
        Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
        alarmIntent.putExtra(Intents.EXTRA_PROFILE,
            smsProfileList.get(profileSpinner.getSelectedItemPosition()));
        alarmIntent.putExtra(Intents.EXTRA_NUMBER, numberText.getText().toString());
        alarmIntent.putExtra(Intents.EXTRA_MESSAGE, messageText.getText().toString());

        getActivity().sendBroadcast(alarmIntent);
      }
    });

    builder.setTitle("Trigger SMS Alarm");
    builder.setView(rootView);
    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
      @Override public void onClick(@NonNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    return builder.create();
  }
}
