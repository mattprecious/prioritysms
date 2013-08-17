package com.mattprecious.prioritysms.fragment;

import com.google.analytics.tracking.android.EasyTracker;

import com.actionbarsherlock.app.SherlockFragment;

public class BaseFragment extends SherlockFragment {
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().setContext(getActivity());
        EasyTracker.getTracker().sendView(getClass().getSimpleName());
    }
}
