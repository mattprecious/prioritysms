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

package com.mattprecious.prioritysms.activity;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.ContactHelper;
import com.mattprecious.prioritysms.util.Intents;
import com.mattprecious.prioritysms.util.Strings;
import com.mattprecious.prioritysms.view.MarginAnimation;
import net.sebastianopoggi.ui.GlowPadBackport.GlowPadView;

public class AlarmActivity extends BaseActivity implements GlowPadView.OnTriggerListener {
  private static final String TAG = AlarmActivity.class.getSimpleName();
  private static final int WHAT_PING_MESSAGE = 101;
  private static final boolean ENABLE_PING_AUTO_REPEAT = true;
  private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

  // mirroring show/hide values from GlowPadView
  private static final int COLLAPSE_ANIMATION_DURATION = 200;
  private static final int COLLAPSE_ANIMATINO_DELAY = 50;
  private static final int EXPAND_ANIMATION_DURATION = 200;
  private static final int EXPAND_ANIMATION_DELAY = 200;

  @InjectView(R.id.contact_name) TextView nameView;
  @InjectView(R.id.message_container) View messageContainerView;
  @InjectView(R.id.message) TextView messageView;
  @InjectView(R.id.image) ImageView iconView;
  @InjectView(R.id.glow_pad_view) GlowPadView glowPadView;

  private BaseProfile profile;
  private SmsProfile smsProfile;
  private String number;
  private String name;
  private String message;

  private boolean pingEnabled = true;
  private boolean animateMessageSize;
  private int messageMarginBottom;

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.v(TAG, "onReceive " + action);

      if (action.equals(Intents.ACTION_DISMISS)) {
        dismiss(false, false);
      } else if (action.equals(Intents.ACTION_REPLY)) {
        reply();
      } else if (action.equals(Intents.ACTION_CALL)) {
        call();
      } else {
        BaseProfile profile = intent.getParcelableExtra(Intents.EXTRA_PROFILE);
        boolean replaced = intent.getBooleanExtra(Intents.ALARM_REPLACED, false);
        if (profile != null && AlarmActivity.this.profile.getId() == profile.getId()) {
          dismiss(true, replaced);
        }
      }
    }
  };

  private final Handler mPingHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case WHAT_PING_MESSAGE:
          triggerPing();
          break;
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (!intent.hasExtra(Intents.EXTRA_PROFILE)) {
      missingExtra(Intents.EXTRA_PROFILE);
    } else if (!intent.hasExtra(Intents.EXTRA_NUMBER)) {
      missingExtra(Intents.EXTRA_NUMBER);
    }

    profile = intent.getParcelableExtra(Intents.EXTRA_PROFILE);
    number = intent.getStringExtra(Intents.EXTRA_NUMBER);
    name = ContactHelper.getNameByNumber(this, number);

    if (profile instanceof SmsProfile) {
      smsProfile = (SmsProfile) profile;
      if (intent.hasExtra(Intents.EXTRA_MESSAGE)) {
        message = intent.getStringExtra(Intents.EXTRA_MESSAGE);
      } else {
        missingExtra(Intents.EXTRA_MESSAGE);
      }
    }

    final LayoutInflater inflater = LayoutInflater.from(this);
    final View rootView = inflater.inflate(R.layout.alarm, null);
    updateSystemUi(rootView);
    setContentView(rootView);
    ButterKnife.inject(this);

    final Window win = getWindow();
    win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

    animateMessageSize =
        !Strings.isBlank(message) && getResources().getBoolean(R.bool.animate_message_size);

    String styledMessage = message;
    if (smsProfile != null) {
      for (String keyword : smsProfile.getKeywords()) {
        styledMessage = styledMessage.replace(keyword, String.format("<b>%s</b>", keyword));
      }

      messageView.setText(Html.fromHtml(styledMessage));
    }

    nameView.setText(name);
    glowPadView.setOnTriggerListener(this);

    iconView.setImageResource(
        (profile instanceof SmsProfile) ? R.drawable.ic_alarm_message : R.drawable.ic_alarm_phone);

    triggerPing();

    IntentFilter filter = new IntentFilter(Intents.ALARM_KILLED);
    filter.addAction(Intents.ACTION_DISMISS);
    filter.addAction(Intents.ACTION_REPLY);
    filter.addAction(Intents.ACTION_CALL);
    registerReceiver(mReceiver, filter);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mReceiver);
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
  }

  @Override public void onBackPressed() {
    // don't allow the activity to be closed
    return;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_DOWN:
      case KeyEvent.KEYCODE_VOLUME_MUTE:
      case KeyEvent.KEYCODE_VOLUME_UP:
        stopService(new Intent(Intents.ACTION_ALERT));
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  private void missingExtra(String extra) {
    throw new IllegalArgumentException(String.format("Missing %s as an intent extra", extra));
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void updateSystemUi(View view) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }
  }

  private void triggerPing() {
    if (pingEnabled) {
      glowPadView.ping();

      if (ENABLE_PING_AUTO_REPEAT) {
        mPingHandler.sendEmptyMessageDelayed(WHAT_PING_MESSAGE, PING_AUTO_REPEAT_DELAY_MSEC);
      }
    }
  }

  private NotificationManager getNotificationManager() {
    return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
  }

  // Dismiss the alarm.
  private void dismiss(boolean killed, boolean replaced) {
    Log.v(TAG, "dismiss");

    Log.i(TAG, "Profile id=" + profile.getId() + (killed ? (replaced ? " replaced" : " killed")
        : " dismissed by user"));
    // The service told us that the alarm has been killed, do not modify
    // the notification or stop the service.
    if (!killed) {
      // Cancel the notification and stop playing the alarm
      NotificationManager nm = getNotificationManager();
      nm.cancel(profile.getId());
      stopService(new Intent(Intents.ACTION_ALERT));
    }
    if (!replaced) {
      finish();
    }
  }

  private void reply() {
    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null)));
    dismiss(false, false);
  }

  private void call() {
    startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)));
    dismiss(false, false);
  }

  @Override public void onFinishFinalAnimation() {
  }

  @Override public void onGrabbed(View v, int handle) {
    pingEnabled = false;

    if (animateMessageSize) {
      messageMarginBottom = getMarginBottom(messageContainerView);

      MarginAnimation animation = new MarginAnimation(messageContainerView);
      animation.setDuration(COLLAPSE_ANIMATION_DURATION);
      animation.setStartOffset(COLLAPSE_ANIMATINO_DELAY);
      animation.setMarginBottom(0);

      messageContainerView.clearAnimation();
      messageContainerView.startAnimation(animation);
    }
  }

  @Override public void onGrabbedStateChange(View v, int handle) {
  }

  @Override public void onReleased(View v, int handle) {
    pingEnabled = true;
    triggerPing();

    if (animateMessageSize) {
      MarginAnimation animation = new MarginAnimation(messageContainerView);
      animation.setDuration(EXPAND_ANIMATION_DURATION);
      animation.setStartOffset(EXPAND_ANIMATION_DELAY);
      animation.setMarginBottom(messageMarginBottom);

      messageContainerView.clearAnimation();
      messageContainerView.startAnimation(animation);
    }
  }

  @Override public void onTrigger(View v, int target) {
    final int resId = glowPadView.getResourceIdForTarget(target);
    switch (resId) {
      case R.drawable.ic_glowpad_call:
        call();
        break;
      case R.drawable.ic_glowpad_message:
        reply();
        break;
      case R.drawable.ic_glowpad_close:
        dismiss(false, false);
        break;
      default:
        break;
    }
  }

  private static int getMarginBottom(View view) {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    return params == null ? 0 : params.bottomMargin;
  }
}
