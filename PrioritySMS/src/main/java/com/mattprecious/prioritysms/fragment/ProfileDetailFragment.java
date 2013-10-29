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

import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import butterknife.InjectView;
import butterknife.Views;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.lang.ref.WeakReference;
import java.util.List;

public class ProfileDetailFragment extends BaseFragment {

    private static final String TAG = ProfileDetailFragment.class.getSimpleName();

    public static final String EXTRA_PROFILE = "profile";

    private static final String STATE_PROFILE = "profile";

    private static final int ERROR_FLAG_NAME = 1;
    private static final int ERROR_FLAG_PAGER = 1 << 1;

    public interface Callbacks {

        public void onNameUpdated(String name);

        public void onDiscard();

        public void onDelete(BaseProfile profile);

        public void onSave();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onNameUpdated(String name) {
        }

        @Override
        public void onDiscard() {
        }

        @Override
        public void onDelete(BaseProfile profile) {
        }

        @Override
        public void onSave() {
        }
    };

    public static ProfileDetailFragment create(BaseProfile profile) {
        Bundle args = new Bundle();
        args.putParcelable(ProfileDetailFragment.EXTRA_PROFILE, profile);

        ProfileDetailFragment fragment = new ProfileDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private Callbacks mCallbacks = sDummyCallbacks;

    private BaseProfile mProfile;

    private int mErrorFlags = 0;
    private int customErrorResId;

    @InjectView(R.id.profile_name_container)
    View mNameContainer;

    @InjectView(R.id.profile_name)
    EditText mNameText;

    @InjectView(R.id.close_rename)
    ImageButton mCloseRenameButton;

    @InjectView(R.id.pager)
    ViewPager mPager;

    @InjectView(R.id.indicator)
    TabPageIndicator mTitleIndicator;

    private ProfilePagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            Bundle args = getArguments();
            if (args == null || !args.containsKey(EXTRA_PROFILE)) {
                throw new IllegalArgumentException(String.format("must provide %s as intent extra",
                        EXTRA_PROFILE));
            } else {
                mProfile = args.getParcelable(EXTRA_PROFILE);
            }
        } else {
            mProfile = savedInstanceState.getParcelable(STATE_PROFILE);
        }

        mPagerAdapter = new ProfilePagerAdapter(getFragmentManager());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = sDummyCallbacks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        Views.inject(this, rootView);

        mCallbacks.onNameUpdated(mProfile.getName());

        mNameContainer.setVisibility((mProfile.getName() == null) ? View.VISIBLE : View.GONE);

        mNameText.setText(mProfile.getName());
        mNameText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateName();
                }
            }

        });
        mNameText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mCallbacks.onNameUpdated(s.toString());
            }
        });

        mCloseRenameButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                closeRename();
            }
        });

        mPager.setAdapter(mPagerAdapter);
        mTitleIndicator.setViewPager(mPager);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        updateProfile();
        outState.putParcelable(STATE_PROFILE, mProfile);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_discard:
                mCallbacks.onDiscard();
                return true;
            case R.id.menu_save:
                validate();
                if (mErrorFlags > 0) {
                    int errorResId = customErrorResId > 0 ? customErrorResId : R.string.detail_error;
                    Crouton.showText(getActivity(), errorResId, Style.ALERT);
                } else {
                    updateProfile();

                    mProfile.save(getActivity());
                    mCallbacks.onSave();
                }

                return true;
            case R.id.menu_delete:
                mProfile.delete(getActivity());

                // the profile could have been updated with local changes, grab
                // the original one
                mCallbacks.onDelete((BaseProfile) getArguments().getParcelable(EXTRA_PROFILE));
                return true;
            case R.id.menu_rename:
                if (mNameContainer.getVisibility() == View.VISIBLE) {
                    closeRename();
                } else {
                    mNameContainer.setVisibility(View.VISIBLE);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateProfile() {
        mProfile.setName(mNameText.getText().toString());
        for (BaseDetailFragment fragment : mPagerAdapter.getRegisteredFragments()) {
            fragment.updateProfile(mProfile);
        }
    }

    private void closeRename() {
        validateName();
        if (!isError(ERROR_FLAG_NAME)) {
            mNameContainer.setVisibility(View.GONE);
        }
    }

    private void setError(int flag) {
        mErrorFlags |= flag;
    }

    private void removeError(int flag) {
        mErrorFlags &= ~flag;
    }

    private boolean isError(int flag) {
        return (mErrorFlags & flag) > 0;
    }

    private void validate() {
        customErrorResId = 0;
        validateName();
        validatePager();
    }

    private void validateName() {
        if (mNameText.getText().length() == 0) {
            mNameText.setError(getString(R.string.detail_error_empty_name));
            setError(ERROR_FLAG_NAME);
        } else {
            mNameText.setError(null);
            removeError(ERROR_FLAG_NAME);
        }
    }

    private void validatePager() {
        int i = 0;
        for (BaseDetailFragment fragment : mPagerAdapter.getRegisteredFragments()) {
            ValidationResponse response = fragment.validate();
            if (!response.isValid()) {
                setError(ERROR_FLAG_PAGER);
                mPager.setCurrentItem(i);

                if (customErrorResId == 0) {
                    customErrorResId = response.getCustomErrorResId();
                }

                return;
            }

            i++;
        }

        removeError(ERROR_FLAG_PAGER);
    }

    private class ProfilePagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_FRAGMENTS = 2;

        SparseArray<WeakReference<Fragment>> mRegisteredFragments = new SparseArray<WeakReference<Fragment>>();
        private final String[] mTitles = new String[NUM_FRAGMENTS];

        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);

            mTitles[0] = getString(R.string.detail_tab_conditions);
            mTitles[1] = getString(R.string.detail_tab_actions);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ProfileDetailConditionsFragment.create(mProfile);
                case 1:
                    return ProfileDetailActionsFragment.create(mProfile);
                default:
                    Log.e(TAG, "invalid getItem position: " + position);
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mRegisteredFragments.put(position, new WeakReference<Fragment>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mRegisteredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position >= 0 && position < NUM_FRAGMENTS) {
                return mTitles[position];
            }

            Log.e(TAG, "invalid getPageTitle position: " + position);
            return null;
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        public List<BaseDetailFragment> getRegisteredFragments() {
            List<BaseDetailFragment> list = Lists.newArrayListWithCapacity(mRegisteredFragments.size());
            for (int i = 0; i < mRegisteredFragments.size(); i++) {
                BaseDetailFragment fragment = getRegisteredFragment(i);
                if (fragment != null) {
                    list.add(fragment);
                }
            }

            return list;
        }

        public BaseDetailFragment getRegisteredFragment(int position) {
            Fragment fragment = mRegisteredFragments.get(position).get();
            if (fragment == null) {
                Log.e(TAG, String.format("fragment at position %d was cleared", position));
                return null;
            }

            return (BaseDetailFragment) fragment;
        }
    }

    public abstract static class BaseDetailFragment extends BaseFragment {

        public abstract void updateProfile(BaseProfile profile);

        public abstract ValidationResponse validate();
    }

    public static class ValidationResponse {
        private boolean valid;
        private int customErrorResId;

        public ValidationResponse(boolean valid) {
            this(valid, 0);
        }

        public ValidationResponse(boolean valid, int customErrorResId) {
            this.valid = valid;
            this.customErrorResId = customErrorResId;
        }

        public boolean isValid() {
            return valid;
        }

        public int getCustomErrorResId() {
            return customErrorResId;
        }
    }
}
