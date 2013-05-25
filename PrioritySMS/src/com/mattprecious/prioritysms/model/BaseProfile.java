
package com.mattprecious.prioritysms.model;

import com.google.common.collect.Sets;

import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.util.ContactHelper;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Set;

public abstract class BaseProfile implements Parcelable {

    private int id;

    private String name;

    private boolean enabled;

    private Uri ringtone;

    private boolean vibrate;

    private Set<String> contacts;

    protected BaseProfile() {
        id = -1;
        enabled = true;
        contacts = Sets.newHashSet();
    }

    public boolean isNew() {
        return id == -1;
    }

    public void save(Context context) {
        DbAdapter db = new DbAdapter(context);

        if (getId() == -1) {
            db.insertProfile(this);
        } else {
            db.updateProfile(this);
        }
    }

    public void delete(Context context) {
        if (getId() == -1) {
            return;
        }

        DbAdapter db = new DbAdapter(context);
        db.deleteProfile(this);
    }

    protected boolean matches(Context context, String number) {
        Set<String> contacts = getContacts();
        if (contacts.size() > 0) {
            String incomingContactId = ContactHelper.getContactIdByNumber(context, number);
            if (incomingContactId == null) {
                return false;
            } else {
                for (String lookupKey : contacts) {
                    String contactId = ContactHelper.getContactIdByLookupKey(context, lookupKey);
                    if (incomingContactId.equals(contactId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int rowId) {
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

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Set<String> getContacts() {
        Set<String> ret = Sets.newHashSet();
        ret.addAll(contacts);
        return ret;
    }

    public void setContacts(Set<String> contacts) {
        this.contacts = Sets.newHashSet();
        this.contacts.addAll(contacts);
    }

    public void addContact(String lookupKey) {
        if (lookupKey != null) {
            contacts.add(lookupKey);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString((ringtone == null) ? null : ringtone.toString());
        dest.writeByte((byte) (vibrate ? 1 : 0));
        dest.writeStringArray(contacts.toArray(new String[contacts.size()]));
    }

    public BaseProfile(Parcel in) {
        id = in.readInt();
        name = in.readString();
        enabled = in.readByte() == 1;

        String ringtoneStr = in.readString();
        ringtone = (ringtoneStr == null) ? null : Uri.parse(ringtoneStr);

        vibrate = in.readByte() == 1;

        String[] contactsArr = in.createStringArray();
        if (contactsArr == null) {
            contacts = Sets.newHashSet();
        } else {
            contacts = Sets.newHashSet(contactsArr);
        }
    }

    @Override
    public String toString() {
        return "BaseProfile [id=" + id + ", name=" + name + ", enabled=" + enabled + ", ringtone="
                + ringtone + ", vibrate=" + vibrate + ", contacts=" + contacts + "]";
    }

}
