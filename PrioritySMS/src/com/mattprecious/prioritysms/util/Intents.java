
package com.mattprecious.prioritysms.util;

public class Intents {
    private Intents() {
    }

    // This action triggers the AlarmReceiver as well as the AlarmService. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String ACTION_ALERT = "com.mattprecious.prioritysms.ALERT";

    // A public action sent by AlarmService when the alarm has stopped sounding
    // for any reason (e.g. because it has been dismissed from AlarmActivity,
    // or killed due to an incoming phone call, etc).
    public static final String ACTION_DONE = "com.mattprecious.prioritysms.DONE";

    // AlarmActivity listens for this broadcast intent, so that other
    // applications
    // can dismiss the alarm (after ACTION_ALERT and before ACTION_DONE).
    public static final String ACTION_DISMISS = "com.mattprecious.prioritysms.DISMISS";

    public static final String ACTION_REPLY = "com.mattprecious.prioritysms.REPLY";

    public static final String ACTION_CALL = "com.mattprecious.prioritysms.CALL";

    // This is a private action used by the AlarmService to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // Extra in the ALARM_KILLED intent to indicate when alarm was replaced
    public static final String ALARM_REPLACED = "alarm_replaced";

    public static final String EXTRA_PROFILE = "profile";
    public static final String EXTRA_NUMBER = "number";
    public static final String EXTRA_MESSAGE = "message";

}
