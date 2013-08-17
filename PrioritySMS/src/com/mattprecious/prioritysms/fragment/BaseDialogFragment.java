package com.mattprecious.prioritysms.fragment;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BaseDialogFragment extends DialogFragment {
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().setContext(getActivity());
        EasyTracker.getTracker().sendView(getClass().getSimpleName());
    }
}
