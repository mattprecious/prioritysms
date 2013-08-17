package com.mattprecious.prioritysms;

import com.crashlytics.android.Crashlytics;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }
    }
}
