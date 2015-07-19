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
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.util.ContactHelper;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseProfile implements Parcelable {
  private int id;
  private String name;
  private boolean enabled;
  private ActionType actionType;
  private Uri ringtone;
  private boolean overrideSilent;
  private boolean vibrate;
  private Set<String> contacts;

  protected BaseProfile() {
    id = -1;
    enabled = true;
    actionType = ActionType.ALARM;
    contacts = new LinkedHashSet<>();
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

    Set<String> incomingContactLookups = ContactHelper.getLookupKeysByNumber(context, number);
    incomingContactLookups.retainAll(contacts);

    return !incomingContactLookups.isEmpty();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public ActionType getActionType() {
    return actionType;
  }

  public void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

  public Uri getRingtone() {
    return ringtone;
  }

  public void setRingtone(Uri ringtone) {
    this.ringtone = ringtone;
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
    Set<String> ret = new LinkedHashSet<>();
    ret.addAll(contacts);
    return ret;
  }

  public void setContacts(Set<String> contacts) {
    this.contacts = new LinkedHashSet<>();
    this.contacts.addAll(contacts);
  }

  public void addContact(String lookupKey) {
    if (lookupKey != null) {
      contacts.add(lookupKey);
    }
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(name);
    dest.writeByte((byte) (enabled ? 1 : 0));
    dest.writeInt(actionType.ordinal());
    dest.writeString((ringtone == null) ? null : ringtone.toString());
    dest.writeByte((byte) (overrideSilent ? 1 : 0));
    dest.writeByte((byte) (vibrate ? 1 : 0));
    dest.writeStringArray(contacts.toArray(new String[contacts.size()]));
  }

  public BaseProfile(Parcel in) {
    id = in.readInt();
    name = in.readString();
    enabled = in.readByte() == 1;

    actionType = ActionType.values()[in.readInt()];

    String ringtoneStr = in.readString();
    ringtone = ringtoneStr == null ? null : Uri.parse(ringtoneStr);

    overrideSilent = in.readByte() == 1;
    vibrate = in.readByte() == 1;

    String[] contactsArr = in.createStringArray();
    if (contactsArr == null) {
      contacts = new LinkedHashSet<>();
    } else {
      contacts = new LinkedHashSet<>(Arrays.asList(contactsArr));
    }
  }

  @Override public String toString() {
    return "BaseProfile{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", enabled=" + enabled +
        ", actionType=" + actionType +
        ", ringtone=" + ringtone +
        ", overrideSilent=" + overrideSilent +
        ", vibrate=" + vibrate +
        ", contacts=" + contacts +
        '}';
  }
}
