
package com.mattprecious.prioritysms.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PhoneProfile extends BaseProfile {
    public PhoneProfile() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public PhoneProfile(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<PhoneProfile> CREATOR = new Parcelable.Creator<PhoneProfile>() {
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
