
package com.mattprecious.prioritysms.model;

import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseProfile {
    private long id;
    private String name;
    private boolean enabled;
    private Uri ringtone;
    private int volume;
    private boolean overrideSilent;
    private boolean vibrate;

    private Set<String> contacts;

    protected BaseProfile() {
        id = -1;
        contacts = new HashSet<String>();
    }

    public long getId() {
        return id;
    }

    public void setId(long rowId) {
        this.id = rowId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Uri getRingtone() {
        return ringtone;
    }

    public void setRingtone(Uri ringtone) {
        this.ringtone = ringtone;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isOverrideSilent() {
        return overrideSilent;
    }

    public void setOverrideSilent(boolean overrideSilent) {
        this.overrideSilent = overrideSilent;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Set<String> getContacts() {
        return contacts;
    }

    public void setContacts(Set<String> contacts) {
        this.contacts = contacts;
    }

    public void addContact(String lookupKey) {
        contacts.add(lookupKey);
    }

    public void removeContact(String lookupKey) {
        contacts.remove(lookupKey);
    }
}
