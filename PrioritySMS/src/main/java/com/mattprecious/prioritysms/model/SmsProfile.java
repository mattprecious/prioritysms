
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
import com.mattprecious.prioritysms.util.Strings;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class SmsProfile extends BaseProfile {

    private LogicMethod mKeywordMethod;
    private Set<String> mKeywords;

    public SmsProfile() {
        mKeywordMethod = LogicMethod.ANY;
        mKeywords = new LinkedHashSet<>();
    }

    public boolean messageMatches(Context context, String number, String message) {
        boolean matches = super.matches(context, number);

        if (!matches) {
            return false;
        }

        if (Strings.isBlank(message)) {
            return false;
        }

        message = message.toLowerCase();

        Set<String> keywords = getKeywords();
        if (keywords.size() > 0) {
            if (getKeywordMethod() == LogicMethod.ALL) {
                matches = true;
                for (String keyword : keywords) {
                    if (!message.contains(keyword.toLowerCase())) {
                        matches = false;
                        break;
                    }
                }
            } else if (getKeywordMethod() == LogicMethod.ONLY) {
                String keyword = keywords.toArray(new String[keywords.size()])[0].toLowerCase();
                matches = keyword.equals(message.trim());
            } else { // if (getKeywordMethod() == LogicMethod.ANY)
                matches = false;
                for (String keyword : keywords) {
                    if (message.contains(keyword.toLowerCase())) {
                        matches = true;
                        break;
                    }
                }
            }
        }

        return matches;
    }

    public LogicMethod getKeywordMethod() {
        return mKeywordMethod;
    }

    public void setKeywordMethod(LogicMethod keywordMethod) {
        mKeywordMethod = keywordMethod;
    }

    public Set<String> getKeywords() {
        Set<String> ret = new LinkedHashSet<>();
        ret.addAll(mKeywords);
        return ret;
    }

    public void setKeywords(Set<String> keywords) {
        mKeywords = new LinkedHashSet<>();
        mKeywords.addAll(keywords);
    }

    public void addKeyword(String keyword) {
        if (keyword != null) {
            mKeywords.add(keyword.trim());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(mKeywordMethod.ordinal());
        dest.writeStringArray(mKeywords.toArray(new String[0]));
    }

    public SmsProfile(Parcel in) {
        super(in);

        mKeywordMethod = LogicMethod.values()[in.readInt()];

        String[] keywordsArr = in.createStringArray();
        if (keywordsArr == null) {
            mKeywords = new LinkedHashSet<>();
        } else {
            mKeywords = new LinkedHashSet<>(Arrays.asList(keywordsArr));
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
        return "SmsProfile [mKeywords=" + mKeywords + ", super.toString()=" + super.toString() +
                "]";
    }
}
