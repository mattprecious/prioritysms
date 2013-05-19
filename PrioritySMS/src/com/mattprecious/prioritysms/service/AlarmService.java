
package com.mattprecious.prioritysms.service;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.util.AlarmAlertWakeLock;
import com.mattprecious.prioritysms.util.Intents;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

public class AlarmService extends Service {

    private static final String TAG = AlarmService.class.getSimpleName();

    // Default of 30 minutes until alarm is silenced.
    private static final String DEFAULT_ALARM_TIMEOUT = "30";

    private static final long[] sVibratePattern = new long[]{
            500, 500
    };

    private boolean mPlaying = false;

    private Vibrator mVibrator;

    private MediaPlayer mMediaPlayer;

    private BaseProfile mCurrentProfile;

    private long mStartTime;

    private TelephonyManager mTelephonyManager;

    private int mInitialCallState;

    // Internal messages
    private static final int KILLER = 1000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    sendKillBroadcast((BaseProfile) msg.obj, false);
                    stopSelf();
                    break;
            }
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (state != TelephonyManager.CALL_STATE_IDLE
                    && state != mInitialCallState) {
                sendKillBroadcast(mCurrentProfile, false);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(
                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
    }

    @Override
    public void onDestroy() {
        stop();
        Intent alarmDone = new Intent(Intents.ACTION_DONE);
        sendBroadcast(alarmDone);

        // Stop listening for incoming calls.
        mTelephonyManager.listen(mPhoneStateListener, 0);
        AlarmAlertWakeLock.releaseCpuLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // No intent, tell the system not to restart us.
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        final BaseProfile profile = intent.getParcelableExtra(
                Intents.EXTRA_PROFILE);

        if (profile == null) {
            Log.v(TAG, "AlarmService failed to parse the alarm from the intent");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (mCurrentProfile != null) {
            sendKillBroadcast(mCurrentProfile, true);
        }

        play(profile);
        mCurrentProfile = profile;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        mInitialCallState = mTelephonyManager.getCallState();

        return START_STICKY;
    }

    private void sendKillBroadcast(BaseProfile profile, boolean replaced) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis /
                (double) DateUtils.MINUTE_IN_MILLIS);
        Intent alarmKilled = new Intent(Intents.ALARM_KILLED);
        alarmKilled.putExtra(Intents.EXTRA_PROFILE, profile);
        alarmKilled.putExtra(Intents.ALARM_KILLED_TIMEOUT, minutes);
        alarmKilled.putExtra(Intents.ALARM_REPLACED, replaced);
        sendBroadcast(alarmKilled);
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    private void play(BaseProfile profile) {
        // stop() checks to see if we are already playing.
        stop();

        Log.v(TAG, "AlarmService.play() " + profile.getId());

        if (profile.getRingtone() != null) {
            Uri ringtone = profile.getRingtone();

            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "Error occurred while playing audio.");
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                if (mTelephonyManager.getCallState()
                        != TelephonyManager.CALL_STATE_IDLE) {
                    Log.v(TAG, "Using the in-call alarm");
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.in_call_alarm);
                } else {
                    mMediaPlayer.setDataSource(this, ringtone);
                }
                startAlarm(mMediaPlayer);
            } catch (Exception ex) {
                Log.v(TAG, "Using the fallback ringtone");
                // The alert may be on the sd card which could be busy right
                // now. Use the fallback ringtone.
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer.reset();
                    setDataSourceFromResource(getResources(), mMediaPlayer,
                            R.raw.fallbackring);
                    startAlarm(mMediaPlayer);
                } catch (Exception ex2) {
                    // At this point we just don't play anything.
                    Log.e(TAG, "Failed to play fallback ringtone", ex2);
                }
            }
        }

        /* Start the vibrator after everything is ok with the media player */
        if (profile.isVibrate()) {
            mVibrator.vibrate(sVibratePattern, 0);
        } else {
            mVibrator.cancel();
        }

        enableKiller(profile);
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    // Do the common stuff when starting the alarm.
    private void startAlarm(MediaPlayer player)
            throws java.io.IOException, IllegalArgumentException,
            IllegalStateException {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }

    private void setDataSourceFromResource(Resources resources,
            MediaPlayer player, int res) throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                    afd.getLength());
            afd.close();
        }
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not repeating
     */
    public void stop() {
        Log.v(TAG, "AlarmService.stop()");
        if (mPlaying) {
            mPlaying = false;

            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // Stop vibrator
            mVibrator.cancel();
        }
        disableKiller();
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm won't run all day. This just
     * cancels the audio, but leaves the notification popped, so the user will know that the alarm
     * tripped.
     */
    private void enableKiller(BaseProfile profile) {
        final String autoSnooze =
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(getString(R.string.pref_key_auto_dismiss),
                                DEFAULT_ALARM_TIMEOUT);
        int autoSnoozeMinutes = Integer.parseInt(autoSnooze);
        if (autoSnoozeMinutes != -1) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, profile),
                    autoSnoozeMinutes * DateUtils.MINUTE_IN_MILLIS);
        }
    }

    private void disableKiller() {
        mHandler.removeMessages(KILLER);
    }

}
