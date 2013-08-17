package com.mattprecious.prioritysms.activity;

import com.google.analytics.tracking.android.EasyTracker;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BaseActivity extends SherlockFragmentActivity {
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
