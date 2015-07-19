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

package com.mattprecious.prioritysms.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class PhoneProfile extends BaseProfile {

  public PhoneProfile() {
  }

  public boolean callMatches(Context context, String number) {
    return super.matches(context, number);
  }

  @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
  }

  public PhoneProfile(Parcel in) {
    super(in);
  }

  public static final Parcelable.Creator<PhoneProfile> CREATOR =
      new Parcelable.Creator<PhoneProfile>() {
        @Override public PhoneProfile createFromParcel(@NonNull Parcel source) {
          return new PhoneProfile(source);
        }

        @NonNull @Override public PhoneProfile[] newArray(int size) {
          return new PhoneProfile[size];
        }
      };
}
