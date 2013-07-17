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
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.util.Intents;

import java.util.List;

public class TriggerAlarmPhoneDialogFragment extends SherlockDialogFragment {

    @InjectView(R.id.profile_spinner)
    Spinner mProfileSpinner;
    @InjectView(R.id.number)
    EditText mNumberText;
    @InjectView(R.id.go)
    Button mGoButton;

    private List<PhoneProfile> mPhoneProfileList;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = inflater.inflate(R.layout.dev_tools_trigger_alarm_phone, null);
        Views.inject(this, rootView);

        mPhoneProfileList = new DbAdapter(getActivity()).getEnabledPhoneProfiles();

        List<String> profileNames = Lists.newArrayListWithCapacity(mPhoneProfileList.size());
        for (BaseProfile profile : mPhoneProfileList) {
            profileNames.add(profile.getName());
        }

        mProfileSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, profileNames));

        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alarmIntent = new Intent(Intents.ACTION_ALERT);
                alarmIntent.putExtra(Intents.EXTRA_PROFILE,
                        mPhoneProfileList.get(mProfileSpinner.getSelectedItemPosition()));
                alarmIntent.putExtra(Intents.EXTRA_NUMBER, mNumberText.getText().toString());

                getActivity().sendBroadcast(alarmIntent);
            }
        });

        builder.setTitle("Trigger Call Alarm");
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
