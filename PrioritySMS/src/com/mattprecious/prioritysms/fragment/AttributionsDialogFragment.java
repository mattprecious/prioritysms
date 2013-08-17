package com.mattprecious.prioritysms.fragment;

import android.app.Dialog;
import android.os.Bundle;
import com.mattprecious.prioritysms.preferences.SettingsActivity;

public class AttributionsDialogFragment extends BaseDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return SettingsActivity.buildAttributionsDialog(getActivity());
    }
}
