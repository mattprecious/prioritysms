
package com.mattprecious.prioritysms.receiver;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mattprecious.prioritysms.activity.AlarmActivity;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.AlarmAlertWakeLock;
import com.mattprecious.prioritysms.util.AsyncHandler;
import com.mattprecious.prioritysms.util.Intents;

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert activity. Passes
 * through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult result = safelyGoAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override
            public void run() {
                handleIntent(context, intent);
                safelyFinishAsync(result);
                wl.release();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private PendingResult safelyGoAsync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return goAsync();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void safelyFinishAsync(PendingResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && result != null) {
            result.finish();
        }
    }

    private void handleIntent(Context context, Intent intent) {
        if (!intent.hasExtra(Intents.EXTRA_PROFILE)) {
            missingExtra(Intents.EXTRA_PROFILE);
        }

        BaseProfile profile = intent.getParcelableExtra(Intents.EXTRA_PROFILE);
        if (Intents.ALARM_KILLED.equals(intent.getAction())) {
            // TODO: throw a notification saying it was auto-killed
            NotificationManager nm = getNotificationManager(context);
            nm.cancel(profile.getId());
            return;
        } else if (!Intents.ACTION_ALERT.equals(intent.getAction())) {
            // Unknown intent, bail.
            return;
        }

        if (!intent.hasExtra(Intents.EXTRA_NUMBER)) {
            missingExtra(Intents.EXTRA_NUMBER);
        } else if (profile instanceof SmsProfile) {
            if (!intent.hasExtra(Intents.EXTRA_MESSAGE)) {
                missingExtra(Intents.EXTRA_MESSAGE);
            }
        }

        String number = intent.getStringExtra(Intents.EXTRA_NUMBER);
        String message = intent.getStringExtra(Intents.EXTRA_MESSAGE);

        Log.v(TAG, "Received alarm set for id=" + profile.getId());

        // Maintain a cpu wake lock until the AlarmActivity and AlarmService can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Play the alarm alert and vibrate the device.
        Intent playAlarm = new Intent(Intents.ACTION_ALERT);
        playAlarm.putExtra(Intents.EXTRA_PROFILE, profile);
        context.startService(playAlarm);

        notifyAlarm(context, profile, number, message);
    }

    private void notifyAlarm(Context context, BaseProfile profile, String number, String message) {
        Intent dismissIntent = new Intent(Intents.ACTION_DISMISS);
        dismissIntent.putExtra(Intents.EXTRA_PROFILE, profile);
        PendingIntent pendingDismiss = PendingIntent.getBroadcast(context,
                profile.getId(), dismissIntent, 0);

        Intent replyIntent = new Intent(Intents.ACTION_REPLY);
        replyIntent.putExtra(Intents.EXTRA_PROFILE, profile);
        PendingIntent pendingReply = PendingIntent.getBroadcast(context,
                profile.getId(), replyIntent, 0);

        Intent callIntent = new Intent(Intents.ACTION_CALL);
        callIntent.putExtra(Intents.EXTRA_PROFILE, profile);
        PendingIntent pendingCall = PendingIntent.getBroadcast(context,
                profile.getId(), callIntent, 0);

        Intent activityIntent = new Intent(context, AlarmActivity.class);
        activityIntent.setAction(String.valueOf(System.currentTimeMillis()));
        activityIntent.putExtra(Intents.EXTRA_PROFILE, profile);
        activityIntent.putExtra(Intents.EXTRA_NUMBER, number);
        activityIntent.putExtra(Intents.EXTRA_MESSAGE, message);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        PendingIntent pendingActivity = PendingIntent.getActivity(context, profile.getId(),
                activityIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(profile.getName())
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        "Dismiss",
                        pendingDismiss)
                .addAction(android.R.drawable.ic_menu_send,
                        "Reply",
                        pendingReply)
                .addAction(android.R.drawable.ic_menu_call,
                        "Call",
                        pendingCall)
                .setFullScreenIntent(pendingActivity, true)
                .setContentIntent(pendingActivity);

        Notification notif = builder.build();

        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        nm.notify(profile.getId(), notif);
    }

    private void missingExtra(String extra) {
        throw new IllegalArgumentException(String.format("Missing %s as an intent extra",
                extra));
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
