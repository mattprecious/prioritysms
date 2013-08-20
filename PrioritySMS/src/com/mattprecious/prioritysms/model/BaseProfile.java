
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

    private int mId;

    private String mName;

    private boolean mEnabled;

    private ActionType mActionType;

    private Uri mRingtone;

    private boolean mOverrideSilent;

    private boolean mVibrate;

    private Set<String> mContacts;

    protected BaseProfile() {
        mId = -1;
        mEnabled = true;
        mActionType = ActionType.ALARM;
        mContacts = Sets.newHashSet();
    }

    public boolean isNew() {
        return mId == -1;
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

    public void undoDelete(Context context) {
        if (getId() == -1) {
            return;
        }

        DbAdapter db = new DbAdapter(context);
        db.insertProfile(this);
    }

    protected boolean matches(Context context, String number) {
        Set<String> contacts = getContacts();

        // no contacts set, so allow all
        if (contacts.size() == 0) {
            return true;
        }

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

        return false;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public ActionType getActionType() {
        return mActionType;
    }

    public void setActionType(ActionType actionType) {
        mActionType = actionType;
    }

    public Uri getRingtone() {
        return mRingtone;
    }

    public void setRingtone(Uri ringtone) {
        mRingtone = ringtone;
    }

    public boolean isOverrideSilent() {
        return mOverrideSilent;
    }

    public void setOverrideSilent(boolean overrideSilent) {
        mOverrideSilent = overrideSilent;
    }

    public boolean isVibrate() {
        return mVibrate;
    }

    public void setVibrate(boolean vibrate) {
        mVibrate = vibrate;
    }

    public Set<String> getContacts() {
        Set<String> ret = Sets.newHashSet();
        ret.addAll(mContacts);
        return ret;
    }

    public void setContacts(Set<String> contacts) {
        mContacts = Sets.newHashSet();
        mContacts.addAll(contacts);
    }

    public void addContact(String lookupKey) {
        if (lookupKey != null) {
            mContacts.add(lookupKey);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
        dest.writeByte((byte) (mEnabled ? 1 : 0));
        dest.writeInt(mActionType.ordinal());
        dest.writeString((mRingtone == null) ? null : mRingtone.toString());
        dest.writeByte((byte) (mOverrideSilent ? 1 : 0));
        dest.writeByte((byte) (mVibrate ? 1 : 0));
        dest.writeStringArray(mContacts.toArray(new String[mContacts.size()]));
    }

    public BaseProfile(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mEnabled = in.readByte() == 1;

        mActionType = ActionType.values()[in.readInt()];

        String ringtoneStr = in.readString();
        mRingtone = (ringtoneStr == null) ? null : Uri.parse(ringtoneStr);

        mOverrideSilent = in.readByte() == 1;
        mVibrate = in.readByte() == 1;

        String[] contactsArr = in.createStringArray();
        if (contactsArr == null) {
            mContacts = Sets.newHashSet();
        } else {
            mContacts = Sets.newHashSet(contactsArr);
        }
    }

    @Override
    public String toString() {
        return "BaseProfile{" +
                "mId=" + mId +
                ", mName='" + mName + '\'' +
                ", mEnabled=" + mEnabled +
                ", mActionType=" + mActionType +
                ", mRingtone=" + mRingtone +
                ", mOverrideSilent=" + mOverrideSilent +
                ", mVibrate=" + mVibrate +
                ", mContacts=" + mContacts +
                '}';
    }

}
