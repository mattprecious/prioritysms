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
import butterknife.InjectView;
import butterknife.Views;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.google.common.collect.Lists;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.Intents;

import java.util.List;

public class TriggerAlarmSmsDialogFragment extends SherlockDialogFragment {

    @InjectView(R.id.profile_spinner)
    Spinner mProfileSpinner;
    @InjectView(R.id.number)
    EditText mNumberText;
    @InjectView(R.id.message)
    EditText mMessageText;
    @InjectView(R.id.go)
    Button mGoButton;

    private List<SmsProfile> mSmsProfileList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = inflater.inflate(R.layout.dev_tools_trigger_alarm_sms, null);
        Views.inject(this, rootView);

        mSmsProfileList = new DbAdapter(getActivity()).getEnabledSmsProfiles();

        List<String> profileNames = Lists.newArrayListWithCapacity(mSmsProfileList.size());
        for (BaseProfile profile : mSmsProfileList) {
            profileNames.add(profile.getName());
        }

        mProfileSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, profileNames));

        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
                alarmIntent.putExtra(Intents.EXTRA_PROFILE,
                        mSmsProfileList.get(mProfileSpinner.getSelectedItemPosition()));
                alarmIntent.putExtra(Intents.EXTRA_NUMBER, mNumberText.getText().toString());
                alarmIntent.putExtra(Intents.EXTRA_MESSAGE, mMessageText.getText().toString());

                getActivity().sendBroadcast(alarmIntent);
            }
        });

        builder.setTitle("Trigger SMS Alarm");
        builder.setView(rootView);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
