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

package com.mattprecious.prioritysms.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.BaseDetailFragment;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.ValidationResponse;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.LogicMethod;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.ContactHelper;
import com.mattprecious.prioritysms.util.Strings;
import java.util.LinkedHashSet;
import java.util.Set;

import static butterknife.ButterKnife.findById;

public class ProfileDetailConditionsFragment extends BaseDetailFragment {
  public static final String EXTRA_PROFILE = "profile";

  public static ProfileDetailConditionsFragment create(BaseProfile profile) {
    Bundle args = new Bundle();
    args.putParcelable(EXTRA_PROFILE, profile);

    ProfileDetailConditionsFragment fragment = new ProfileDetailConditionsFragment();
    fragment.setArguments(args);
    return fragment;
  }

  private static final int REQUEST_CONTACT_PICKER = 1;
  private static final int NUM_CHILDREN_CONTACTS_LIST = 3;
  private static final int NUM_CHILDREN_KEYWORDS_LIST = 3;

  @InjectView(R.id.contact_list) ViewGroup contactsList;
  @InjectView(R.id.no_contacts) TextView noContactsView;
  @InjectView(R.id.add_contact) Button addContactButton;
  @InjectView(R.id.keywords_container) ViewGroup keywordsContainer;
  @InjectView(R.id.keyword_method) TextView keywordMethodButton;
  @InjectView(R.id.keyword_list) ViewGroup keywordsList;
  @InjectView(R.id.no_keywords) TextView noKeywordsView;
  @InjectView(R.id.add_keyword) Button addKeywordButton;

  private LayoutInflater inflater;
  private BaseProfile profile;
  private SmsProfile smsProfile;
  // private PhoneProfile phoneProfile;

  private ContactViewHolder contactPickerSource;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null && args.containsKey(EXTRA_PROFILE)) {
      profile = args.getParcelable(EXTRA_PROFILE);
      if (profile instanceof SmsProfile) {
        smsProfile = (SmsProfile) profile;
        // } else {
        // mPhoneProfile = (PhoneProfile) profile;
      }
    } else {
      throw new IllegalArgumentException(
          String.format("must provide %s as intent extra", EXTRA_PROFILE));
    }

    inflater = LayoutInflater.from(getActivity());
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.profile_detail_conditions, container, false);
    ButterKnife.inject(this, rootView);

    for (String contact : profile.getContacts()) {
      addContact(contact);
    }

    if (smsProfile != null) {
      keywordsContainer.setVisibility(View.VISIBLE);
      for (String keyword : smsProfile.getKeywords()) {
        addKeyword(keyword);
      }

      keywordMethodButton.setOnClickListener(keywordMethodListener);
      updateKeywordMethod();
      updateAddKeywordButton();
    }

    addContactButton.setOnClickListener(addContactListener);
    addKeywordButton.setOnClickListener(addKeywordListener);

    return rootView;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CONTACT_PICKER:
        if (resultCode == Activity.RESULT_OK) {
          String lookup = ContactHelper.getLookupKeyByUri(getActivity(), data.getData());
          if (contactPickerSource == null) {
            addContact(lookup);
          } else {
            updateContact(contactPickerSource, lookup);
          }
        }

        contactPickerSource = null;
        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
        break;
    }
  }

  @Override public void updateProfile(BaseProfile profile) {
    Set<String> contacts = new LinkedHashSet<>();
    for (int i = 0; i < contactsList.getChildCount() - NUM_CHILDREN_CONTACTS_LIST; i++) {
      View v = contactsList.getChildAt(i);
      contacts.add(((ContactViewHolder) v.getTag()).lookup);
    }

    profile.setContacts(contacts);

    if (profile instanceof SmsProfile) {
      SmsProfile smsProfile = (SmsProfile) profile;

      smsProfile.setKeywordMethod(this.smsProfile.getKeywordMethod());

      Set<String> keywords = new LinkedHashSet<>();
      for (int i = 0; i < keywordsList.getChildCount() - NUM_CHILDREN_KEYWORDS_LIST; i++) {
        View v = keywordsList.getChildAt(i);
        String keyword = ((TextView) v.getTag()).getText().toString();
        if (!Strings.isBlank(keyword)) {
          keywords.add(keyword.trim());
        }
      }

      smsProfile.setKeywords(keywords);
    }
  }

  @Override public ValidationResponse validate() {
    updateProfile(profile);

    if (smsProfile != null) {
      String keywordError = null;
      if (smsProfile.getKeywordMethod() == LogicMethod.ONLY
          && keywordsList.getChildCount() - NUM_CHILDREN_KEYWORDS_LIST > 1) {
        keywordError = getString(R.string.conditions_error_only_many_keywords);
      }

      if (keywordsList.getChildCount() > NUM_CHILDREN_KEYWORDS_LIST) {
        ((EditText) keywordsList.getChildAt(0).getTag()).setError(null);
      }

      for (int i = 1; i < keywordsList.getChildCount() - NUM_CHILDREN_KEYWORDS_LIST; i++) {
        ((EditText) keywordsList.getChildAt(i).getTag()).setError(keywordError);
      }

      if (keywordError != null) {
        return new ValidationResponse(false);
      }
    }

    boolean isConditionSet = profile.getContacts().size() > 0;
    if (!isConditionSet) {
      isConditionSet = smsProfile != null && smsProfile.getKeywords().size() > 0;
    }

    if (isConditionSet) {
      return new ValidationResponse(true);
    } else {
      return new ValidationResponse(false,
          smsProfile == null ? R.string.conditions_error_no_contacts
              : R.string.conditions_error_no_conditions);
    }
  }

  private void addContact(String contactLookup) {
    noContactsView.setVisibility(View.GONE);

    View v = inflater.inflate(R.layout.profile_detail_contact_item, contactsList, false);

    ContactViewHolder holder = new ContactViewHolder(v);
    v.setTag(holder);
    v.setOnClickListener(contactClickListener);

    updateContact(holder, contactLookup);

    holder.delete.setOnClickListener(contactDeleteListener);

    // set the tag to the main view so we can easily delete
    holder.delete.setTag(v);

    contactsList.addView(v, contactsList.getChildCount() - NUM_CHILDREN_CONTACTS_LIST);
  }

  private void addKeyword(String keyword) {
    noKeywordsView.setVisibility(View.GONE);

    View v = inflater.inflate(R.layout.profile_detail_keyword_item, keywordsList, false);

    EditText nameText = findById(v, R.id.text);
    nameText.setText(keyword);

    // GB bug: Android caches the value by id when saving the state, and
    // since each keyword EditText has the same id they will all be
    // restored to the same value after a rotation
    nameText.setSaveEnabled(false);

    ImageButton deleteButton = findById(v, R.id.delete);
    deleteButton.setOnClickListener(keywordDeleteListener);

    // set the tag to the main view so we can easily delete
    deleteButton.setTag(v);

    // set data holder view as the tag on the main view so we don't
    // have to do any lookups as we loop through to save
    v.setTag(nameText);
    keywordsList.addView(v, keywordsList.getChildCount() - NUM_CHILDREN_KEYWORDS_LIST);

    // GB bug: need to request focus AFTER it's been added to a parent
    nameText.requestFocus();
  }

  private void updateContact(ContactViewHolder holder, String contactLookup) {
    try {
      holder.name.setText(ContactHelper.getNameByLookupKey(getActivity(), contactLookup));
    } catch (IllegalArgumentException e) {
      holder.name.setText(R.string.conditions_contact_not_found);
    }

    holder.avatar.setImageBitmap(ContactHelper.getContactPhoto(getActivity(), contactLookup));

    // this view will store the contact lookup
    holder.lookup = contactLookup;
  }

  private void openContactPicker() {
    startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI),
        REQUEST_CONTACT_PICKER);
  }

  private void updateKeywordMethod() {
    int methodResId;

    switch (smsProfile.getKeywordMethod()) {
      case ALL:
        methodResId = R.string.conditions_keywords_method_all;
        break;
      case ONLY:
        methodResId = R.string.conditions_keywords_method_only;
        break;
      case ANY:
      default:
        methodResId = R.string.conditions_keywords_method_any;
        break;
    }

    keywordMethodButton.setText(methodResId);
  }

  private void updateAddKeywordButton() {
    if (smsProfile != null) {
      boolean enabled = smsProfile.getKeywordMethod() != LogicMethod.ONLY
          || keywordsList.getChildCount() < NUM_CHILDREN_KEYWORDS_LIST + 1;
      addKeywordButton.setEnabled(enabled);
    }
  }

  public static class ContactViewHolder {
    @InjectView(R.id.avatar) ImageView avatar;
    @InjectView(R.id.name) TextView name;
    @InjectView(R.id.delete) ImageButton delete;

    String lookup;

    public ContactViewHolder(View view) {
      ButterKnife.inject(this, view);
    }
  }

  private OnClickListener addContactListener = new OnClickListener() {

    @Override public void onClick(View v) {
      openContactPicker();
    }
  };

  private OnClickListener keywordMethodListener = new OnClickListener() {

    @Override public void onClick(View v) {
      SmsMethodDialogFragment methodFragment =
          SmsMethodDialogFragment.create(smsProfile.getKeywordMethod());
      methodFragment.setCallbacks(keywordMethodCallback);
      methodFragment.show(getFragmentManager(), null);
    }
  };

  private SmsMethodDialogFragment.Callbacks keywordMethodCallback =
      new SmsMethodDialogFragment.Callbacks() {

        @Override public void onSelected(LogicMethod method) {
          smsProfile.setKeywordMethod(method);
          updateKeywordMethod();
          updateAddKeywordButton();
          validate();
        }
      };

  private OnClickListener addKeywordListener = new OnClickListener() {

    @Override public void onClick(View v) {
      addKeyword(null);
      updateAddKeywordButton();
    }
  };

  private OnClickListener contactClickListener = new OnClickListener() {

    @Override public void onClick(View v) {
      contactPickerSource = (ContactViewHolder) v.getTag();
      openContactPicker();
    }
  };

  private OnClickListener contactDeleteListener = new OnClickListener() {

    @Override public void onClick(View v) {
      View viewToRemove = (View) v.getTag();
      ((ViewGroup) viewToRemove.getParent()).removeView(viewToRemove);

      validate();

      if (contactsList.getChildCount() == NUM_CHILDREN_CONTACTS_LIST) {
        noContactsView.setVisibility(View.VISIBLE);
      }
    }
  };

  private OnClickListener keywordDeleteListener = new OnClickListener() {

    @Override public void onClick(View v) {
      View viewToRemove = (View) v.getTag();
      ((ViewGroup) viewToRemove.getParent()).removeView(viewToRemove);

      updateAddKeywordButton();
      validate();

      if (keywordsList.getChildCount() == NUM_CHILDREN_KEYWORDS_LIST) {
        noKeywordsView.setVisibility(View.VISIBLE);
      }
    }
  };
}
