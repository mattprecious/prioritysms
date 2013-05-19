
package com.mattprecious.prioritysms.model;

import com.google.common.collect.Sets;

import com.mattprecious.prioritysms.util.ContactHelper;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Set;

public class SmsProfile extends BaseProfile {

    private Set<String> keywords;

    public SmsProfile() {
        keywords = Sets.newHashSet();
    }

    public boolean messageMatches(Context context, String number, String message) {
        boolean matches = true;

        Set<String> contacts = getContacts();
        if (contacts.size() > 0) {
            matches = false;

            String incomingContactId = ContactHelper.getContactIdByNumber(context, number);
            if (incomingContactId == null) {
                matches = false;
            } else {
                for (String lookupKey : contacts) {
                    String contactId = ContactHelper.getContactIdByLookupKey(context, lookupKey);
                    if (incomingContactId.equals(contactId)) {
                        matches = true;
                        break;
                    }
                }
            }
        }

        if (!matches) {
            return false;
        }

        Set<String> keywords = getKeywords();
        if (keywords.size() > 0) {
            matches = false;

            for (String keyword : keywords) {
                if (message.contains(keyword)) {
                    matches = true;
                    break;
                }
            }
        }

        return matches;
    }

    public Set<String> getKeywords() {
        Set<String> ret = Sets.newHashSet();
        ret.addAll(keywords);
        return ret;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = Sets.newHashSet();
        this.keywords.addAll(keywords);
    }

    public void addKeyword(String keyword) {
        if (keyword != null) {
            keywords.add(keyword);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeStringArray(keywords.toArray(new String[0]));
    }

    public SmsProfile(Parcel in) {
        super(in);

        String[] keywordsArr = in.createStringArray();
        if (keywordsArr == null) {
            keywords = Sets.newHashSet();
        } else {
            keywords = Sets.newHashSet(keywordsArr);
        }
    }

    public static final Parcelable.Creator<SmsProfile> CREATOR
            = new Parcelable.Creator<SmsProfile>() {
        @Override
        public SmsProfile createFromParcel(Parcel source) {
            return new SmsProfile(source);
        }

        @Override
        public SmsProfile[] newArray(int size) {
            return new SmsProfile[size];
        }
    };

    @Override
    public String toString() {
        return "SmsProfile [keywords=" + keywords + ", super.toString()=" + super.toString() + "]";
    }
}
