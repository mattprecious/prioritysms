
package com.mattprecious.prioritysms.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Sets;

import java.util.Set;

public class SmsProfile extends BaseProfile {
    private Set<String> keywords;

    public SmsProfile() {
        keywords = Sets.newHashSet();
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

    public void removeKeyword(String keyword) {
        if (keyword != null) {
            keywords.remove(keyword);
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

    public static final Parcelable.Creator<SmsProfile> CREATOR = new Parcelable.Creator<SmsProfile>() {
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
