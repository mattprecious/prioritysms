
package com.mattprecious.prioritysms.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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

public class ProfileDetailFragment extends SherlockFragment {

    private static final String TAG = ProfileDetailFragment.class.getSimpleName();

    public static final String EXTRA_PROFILE = "profile";

    private static final int ERROR_FLAG_NAME = 1 << 0;
    private static final int ERROR_FLAG_PAGER = 1 << 1;

    public interface Callbacks {

        public void onNameUpdated(String name);

        public void onDiscard();

        public void onDelete();

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
        public void onDelete() {
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

        Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_PROFILE)) {
            throw new IllegalArgumentException(String.format("must provide %s as intent extra",
                    EXTRA_PROFILE));
        } else {
            mProfile = args.getParcelable(EXTRA_PROFILE);
        }
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

        mPagerAdapter = new ProfilePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTitleIndicator.setViewPager(mPager);

        return rootView;
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
                    Crouton.showText(getActivity(), R.string.detail_error, Style.ALERT);
                } else {
                    mProfile.setName(mNameText.getText().toString());
                    for (BaseDetailFragment fragment : mPagerAdapter.getItems()) {
                        fragment.updateProfile(mProfile);
                    }

                    mProfile.save(getActivity());
                    mCallbacks.onSave();
                }

                return true;
            case R.id.menu_delete:
                mProfile.delete(getActivity());
                mCallbacks.onDelete();
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
        BaseDetailFragment[] fragments = mPagerAdapter.getItems();
        for (int i = 0; i < fragments.length; i++) {
            if (!fragments[i].validate()) {
                setError(ERROR_FLAG_PAGER);
                mPager.setCurrentItem(i);
                return;
            }
        }

        removeError(ERROR_FLAG_PAGER);
    }

    private class ProfilePagerAdapter extends FragmentStatePagerAdapter {

        private final int NUM_FRAGMENTS = 2;

        private final BaseDetailFragment[] FRAGMENTS = new BaseDetailFragment[NUM_FRAGMENTS];

        private final String[] TITLES = new String[NUM_FRAGMENTS];

        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);

            TITLES[0] = getString(R.string.detail_tab_conditions);
            FRAGMENTS[0] = ProfileDetailConditionsFragment.create(mProfile);

            TITLES[1] = getString(R.string.detail_tab_actions);
            FRAGMENTS[1] = ProfileDetailActionsFragment.create(mProfile);
        }

        public BaseDetailFragment[] getItems() {
            return FRAGMENTS;
        }

        @Override
        public Fragment getItem(int position) {
            if (position >= 0 && position < NUM_FRAGMENTS) {
                return FRAGMENTS[position];
            }

            Log.e(TAG, "invalid getItem position: " + position);
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position >= 0 && position < NUM_FRAGMENTS) {
                return TITLES[position];
            }

            Log.e(TAG, "invalid getPageTitle position: " + position);
            return null;
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

    }

    public abstract static class BaseDetailFragment extends SherlockFragment {

        public abstract void updateProfile(BaseProfile profile);

        public abstract boolean validate();
    }
}
