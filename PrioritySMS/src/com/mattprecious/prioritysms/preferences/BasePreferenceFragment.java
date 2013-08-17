package com.mattprecious.prioritysms.preferences;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BasePreferenceFragment extends PreferenceFragment {
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().setContext(getActivity());
        EasyTracker.getTracker().sendView(getClass().getSimpleName());
    }
}
