
package com.mattprecious.prioritysms.model;

import com.mattprecious.prioritysms.util.ContactHelper;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Set;

public class PhoneProfile extends BaseProfile {

    public PhoneProfile() {
    }

    public boolean callMatches(Context context, String number) {
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public PhoneProfile(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<PhoneProfile> CREATOR
            = new Parcelable.Creator<PhoneProfile>() {
        @Override
        public PhoneProfile createFromParcel(Parcel source) {
            return new PhoneProfile(source);
        }

        @Override
        public PhoneProfile[] newArray(int size) {
            return new PhoneProfile[size];
        }
    };
}
