
package com.mattprecious.prioritysms.fragment;

import android.widget.ImageView;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.BaseDetailFragment;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.LogicMethod;
import com.mattprecious.prioritysms.model.SmsProfile;
import com.mattprecious.prioritysms.util.ContactHelper;

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

import java.util.Set;

import butterknife.InjectView;
import butterknife.Views;

import static butterknife.Views.findById;

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
    Button mAddContactButton;

    @InjectView(R.id.keyword_method)
    TextView mKeywordMethodButton;

    @InjectView(R.id.add_keyword)
    Button mAddKeywordButton;

    private ContactViewHolder mContactPickerSource;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
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

            mKeywordMethodButton.setOnClickListener(keywordMethodListener);
            updateKeywordMethod();
        }

        mAddContactButton.setOnClickListener(addContactListener);
        mAddKeywordButton.setOnClickListener(addKeywordListener);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONTACT_PICKER:
                if (resultCode == Activity.RESULT_OK) {
                    String lookup = ContactHelper.getLookupKeyByUri(getActivity(), data.getData());
                    if (mContactPickerSource == null) {
                        addContact(lookup);
                    } else {
                        updateContact(mContactPickerSource, lookup);
                    }
                }

                mContactPickerSource = null;
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
            contacts.add(((ContactViewHolder) v.getTag()).lookup);
        }

        profile.setContacts(contacts);

        if (profile instanceof SmsProfile) {
            SmsProfile smsProfile = (SmsProfile) profile;

            smsProfile.setKeywordMethod(mSmsProfile.getKeywordMethod());

            Set<String> keywords = Sets.newHashSet();
            for (int i = 0; i < mKeywordsList.getChildCount(); i++) {
                View v = mKeywordsList.getChildAt(i);
                String keyword = ((TextView) v.getTag()).getText().toString();
                if (!Strings.isNullOrEmpty(keyword)) {
                    keywords.add(keyword.trim());
                }
            }

            smsProfile.setKeywords(keywords);
        }
    }

    private void addContact(String contactLookup) {
        View v = mInflater.inflate(R.layout.profile_detail_contact_item, mContactsList, false);

        ContactViewHolder holder = new ContactViewHolder(v);
        v.setTag(holder);
        v.setOnClickListener(contactClickListener);

        updateContact(holder, contactLookup);

        holder.delete.setOnClickListener(deleteListener);

        // set the tag to the main view so we can easily delete
        holder.delete.setTag(v);

        mContactsList.addView(v);
    }

    private void addKeyword(String keyword) {
        View v = mInflater.inflate(R.layout.profile_detail_keyword_item, mKeywordsList, false);

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

        switch (mSmsProfile.getKeywordMethod()) {
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

        mKeywordMethodButton.setText(methodResId);
    }

    public static class ContactViewHolder {
        @InjectView(R.id.avatar)
        ImageView avatar;
        @InjectView(R.id.name)
        TextView name;
        @InjectView(R.id.delete)
        ImageButton delete;

        String lookup;

        public ContactViewHolder(View view) {
            Views.inject(this, view);
        }
    }

    private OnClickListener addContactListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            openContactPicker();
        }
    };

    private OnClickListener keywordMethodListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            SmsMethodDialogFragment methodFragment = SmsMethodDialogFragment
                    .create(mSmsProfile.getKeywordMethod());
            methodFragment.setCallbacks(mKeywordMethodCallback);
            methodFragment.show(getFragmentManager(), null);
        }
    };

    private SmsMethodDialogFragment.Callbacks mKeywordMethodCallback =
            new SmsMethodDialogFragment.Callbacks() {

                @Override
                public void onSelected(LogicMethod method) {
                    mSmsProfile.setKeywordMethod(method);
                    updateKeywordMethod();
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
            mContactPickerSource = (ContactViewHolder) v.getTag();
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
