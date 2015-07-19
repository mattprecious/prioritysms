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
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.util.Intents;
import java.util.ArrayList;
import java.util.List;

public class TriggerAlarmPhoneDialogFragment extends SherlockDialogFragment {

  @Bind(R.id.profile_spinner) Spinner profileSpinner;
  @Bind(R.id.number) EditText numberText;
  @Bind(R.id.go) Button goButton;

  private List<PhoneProfile> phoneProfileList;

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    View rootView = inflater.inflate(R.layout.dev_tools_trigger_alarm_phone, null);
    ButterKnife.bind(this, rootView);

    phoneProfileList = new DbAdapter(getActivity()).getEnabledPhoneProfiles();

    List<String> profileNames = new ArrayList<>(phoneProfileList.size());
    for (BaseProfile profile : phoneProfileList) {
      profileNames.add(profile.getName());
    }

    profileSpinner.setAdapter(
        new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
            profileNames)
    );

    goButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
        alarmIntent.putExtra(Intents.EXTRA_PROFILE,
            phoneProfileList.get(profileSpinner.getSelectedItemPosition()));
        alarmIntent.putExtra(Intents.EXTRA_NUMBER, numberText.getText().toString());

        getActivity().sendBroadcast(alarmIntent);
      }
    });

    builder.setTitle("Trigger Call Alarm");
    builder.setView(rootView);
    builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    return builder.create();
  }
}
