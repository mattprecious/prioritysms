package com.mattprecious.prioritysms;

import com.crashlytics.android.Crashlytics;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.mattprecious.prioritysms.util.Intents;

import android.app.Application;
import android.content.Intent;

import java.lang.Thread.UncaughtExceptionHandler;

public class MyApplication extends Application {

    private UncaughtExceptionHandler mExceptionHandler = new UncaughtExceptionHandler() {
        private UncaughtExceptionHandler mOriginalHandler = Thread
                .getDefaultUncaughtExceptionHandler();

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            stopService(new Intent(Intents.ACTION_ALERT));
            mOriginalHandler.uncaughtException(thread, ex);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            GoogleAnalytics.getInstance(this).setDebug(true);
        } else {
            Crashlytics.start(this);
        }

        Thread.setDefaultUncaughtExceptionHandler(mExceptionHandler);
    }
}
