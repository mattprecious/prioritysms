package com.mattprecious.prioritysms.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import com.mattprecious.prioritysms.preferences.SettingsActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AttributionsDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return SettingsActivity.buildAttributionsDialog(getActivity());
    }
}
