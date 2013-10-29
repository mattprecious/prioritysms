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
