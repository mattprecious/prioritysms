
package com.mattprecious.prioritysms.fragment;

import static butterknife.Views.findById;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.Views;

import com.google.common.collect.Sets;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.BaseDetailFragment;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.ContactHelper;

import java.util.Set;

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

    private LayoutInflater mInflater;
    private BaseProfile mProfile;
    private SmsProfile mSmsProfile;
    // private PhoneProfile mPhoneProfile;

    @InjectView(R.id.contact_list)
    ViewGroup mContactsList;
    @InjectView(R.id.keywords_container)
    ViewGroup mKeywordsContainer;
    @InjectView(R.id.keyword_list)
    ViewGroup mKeywordsList;
    @InjectView(R.id.add_contact)
    Button addContactButton;
    @InjectView(R.id.add_keyword)
    Button addKeywordButton;

    private View contactPickerSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_PROFILE)) {
            mProfile = args.getParcelable(EXTRA_PROFILE);
            if (mProfile instanceof SmsProfile) {
                mSmsProfile = (SmsProfile) mProfile;
                // } else {
                // mPhoneProfile = (PhoneProfile) mProfile;
            }
        } else {
            throw new IllegalArgumentException(String.format("must provide %s as intent extra",
                    EXTRA_PROFILE));
        }

        mInflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_detail_conditions, container, false);
        Views.inject(this, rootView);

        for (String contact : mProfile.getContacts()) {
            addContact(contact);
        }

        if (mSmsProfile != null) {
            mKeywordsContainer.setVisibility(View.VISIBLE);
            for (String keyword : mSmsProfile.getKeywords()) {
                addKeyword(keyword);
            }
        }

        addContactButton.setOnClickListener(addContactListener);
        addKeywordButton.setOnClickListener(addKeywordListener);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONTACT_PICKER:
                if (resultCode == Activity.RESULT_OK) {
                    String lookup = ContactHelper.getLookupKeyByUri(getActivity(), data.getData());
                    if (contactPickerSource == null) {
                        addContact(lookup);
                    } else {
                        updateContact((TextView) contactPickerSource, lookup);
                    }
                }

                contactPickerSource = null;
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void updateProfile(BaseProfile profile) {
        Set<String> contacts = Sets.newHashSet();
        for (int i = 0; i < mContactsList.getChildCount(); i++) {
            View v = mContactsList.getChildAt(i);
            contacts.add((String) ((View) v.getTag()).getTag());
        }

        profile.setContacts(contacts);

        if (profile instanceof SmsProfile) {
            SmsProfile smsProfile = (SmsProfile) profile;

            Set<String> keywords = Sets.newHashSet();
            for (int i = 0; i < mKeywordsList.getChildCount(); i++) {
                View v = mKeywordsList.getChildAt(i);
                String keyword = ((TextView) v.getTag()).getText().toString();
                if (keyword != null && keyword.length() > 0) {
                    keywords.add(keyword);
                }
            }

            smsProfile.setKeywords(keywords);
        }
    }

    private void addContact(String contactLookup) {
        View v = mInflater.inflate(R.layout.profile_detail_contact_item, mContactsList, false);

        TextView nameText = findById(v, R.id.text);
        nameText.setOnClickListener(contactClickListener);
        updateContact(nameText, contactLookup);

        ImageButton deleteButton = findById(v, R.id.delete);
        deleteButton.setOnClickListener(deleteListener);

        // set the tag to the main view so we can easily delete
        deleteButton.setTag(v);

        // set data holder view as the tag on the main view so we don't have
        // to do any lookups as we loop through to save
        v.setTag(nameText);
        mContactsList.addView(v);
    }

    private void addKeyword(String keyword) {
        View v = mInflater.inflate(R.layout.profile_detail_keyword_item, mKeywordsList,
                false);

        TextView nameText = findById(v, R.id.text);
        nameText.setText(keyword);

        ImageButton deleteButton = findById(v, R.id.delete);
        deleteButton.setOnClickListener(deleteListener);

        // set the tag to the main view so we can easily delete
        deleteButton.setTag(v);

        // set data holder view as the tag on the main view so we don't
        // have to do any lookups as we loop through to save
        v.setTag(nameText);
        mKeywordsList.addView(v);
    }

    private void updateContact(TextView v, String contactLookup) {
        try {
            v.setText(ContactHelper.getNameByLookupKey(getActivity(), contactLookup));
        } catch (IllegalArgumentException e) {
            v.setText(R.string.conditions_contact_not_found);
        }

        // this view will store the contact lookup
        v.setTag(contactLookup);
    }

    private void openContactPicker() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI),
                REQUEST_CONTACT_PICKER);
    }

    private OnClickListener addContactListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            openContactPicker();
        }
    };

    private OnClickListener addKeywordListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            addKeyword(null);
        }
    };

    private OnClickListener contactClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            contactPickerSource = v;
            openContactPicker();
        }
    };

    private OnClickListener deleteListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            View viewToRemove = (View) v.getTag();
            ((ViewGroup) viewToRemove.getParent()).removeView(viewToRemove);
        }

    };
}
