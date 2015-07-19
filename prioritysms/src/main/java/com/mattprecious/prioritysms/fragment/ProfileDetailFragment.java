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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.viewpagerindicator.TabPageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ProfileDetailFragment extends BaseFragment {
  public static final String EXTRA_PROFILE = "profile";

  private static final String TAG = ProfileDetailFragment.class.getSimpleName();
  private static final String STATE_PROFILE = "profile";

  private static final int ERROR_FLAG_NAME = 1;
  private static final int ERROR_FLAG_PAGER = 1 << 1;

  public interface Callbacks {
    public void onNameUpdated(String name);
    public void onDiscard();
    public void onDelete(BaseProfile profile);
    public void onSave();
  }

  private static Callbacks dummyCallbacks = new Callbacks() {
    @Override public void onNameUpdated(String name) {}
    @Override public void onDiscard() {}
    @Override public void onDelete(BaseProfile profile) {}
    @Override public void onSave() {}
  };

  public static ProfileDetailFragment create(BaseProfile profile) {
    Bundle args = new Bundle();
    args.putParcelable(ProfileDetailFragment.EXTRA_PROFILE, profile);

    ProfileDetailFragment fragment = new ProfileDetailFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Bind(R.id.profile_name_container) View nameContainer;
  @Bind(R.id.profile_name) EditText nameText;
  @Bind(R.id.close_rename) ImageButton closeRenameButton;
  @Bind(R.id.pager) ViewPager pager;
  @Bind(R.id.indicator) TabPageIndicator titleIndicator;

  private Callbacks callbacks = dummyCallbacks;
  private BaseProfile profile;
  private ProfilePagerAdapter pagerAdapter;

  private int errorFlags = 0;
  private int customErrorResId;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    if (savedInstanceState == null) {
      Bundle args = getArguments();
      if (args == null || !args.containsKey(EXTRA_PROFILE)) {
        throw new IllegalArgumentException(
            String.format("must provide %s as intent extra", EXTRA_PROFILE));
      } else {
        profile = args.getParcelable(EXTRA_PROFILE);
      }
    } else {
      profile = savedInstanceState.getParcelable(STATE_PROFILE);
    }

    pagerAdapter = new ProfilePagerAdapter(getFragmentManager());
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (!(activity instanceof Callbacks)) {
      throw new IllegalStateException("Activity must implement fragment's callbacks.");
    }

    callbacks = (Callbacks) activity;
  }

  @Override public void onDetach() {
    super.onDetach();

    callbacks = dummyCallbacks;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_profile_detail, container, false);
    ButterKnife.bind(this, rootView);

    callbacks.onNameUpdated(profile.getName());

    nameContainer.setVisibility((profile.getName() == null) ? View.VISIBLE : View.GONE);

    nameText.setText(profile.getName());
    nameText.setOnFocusChangeListener(new OnFocusChangeListener() {

      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validateName();
        }
      }
    });
    nameText.addTextChangedListener(new TextWatcher() {

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override public void afterTextChanged(Editable s) {
        callbacks.onNameUpdated(s.toString());
      }
    });

    closeRenameButton.setOnClickListener(new OnClickListener() {

      @Override public void onClick(View v) {
        closeRename();
      }
    });

    pager.setAdapter(pagerAdapter);
    titleIndicator.setViewPager(pager);

    return rootView;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    updateProfile();
    outState.putParcelable(STATE_PROFILE, profile);
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_profile_detail, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_discard:
        callbacks.onDiscard();
        return true;
      case R.id.menu_save:
        validate();
        if (errorFlags > 0) {
          int errorResId = customErrorResId > 0 ? customErrorResId : R.string.detail_error;
          Crouton.showText(getActivity(), errorResId, Style.ALERT);
        } else {
          updateProfile();

          profile.save(getActivity());
          callbacks.onSave();
        }

        return true;
      case R.id.menu_delete:
        profile.delete(getActivity());

        // the profile could have been updated with local changes, grab
        // the original one
        callbacks.onDelete((BaseProfile) getArguments().getParcelable(EXTRA_PROFILE));
        return true;
      case R.id.menu_rename:
        if (nameContainer.getVisibility() == View.VISIBLE) {
          closeRename();
        } else {
          nameContainer.setVisibility(View.VISIBLE);
        }

        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void updateProfile() {
    profile.setName(nameText.getText().toString());
    for (BaseDetailFragment fragment : pagerAdapter.getRegisteredFragments()) {
      fragment.updateProfile(profile);
    }
  }

  private void closeRename() {
    validateName();
    if (!isError(ERROR_FLAG_NAME)) {
      nameContainer.setVisibility(View.GONE);
    }
  }

  private void setError(int flag) {
    errorFlags |= flag;
  }

  private void removeError(int flag) {
    errorFlags &= ~flag;
  }

  private boolean isError(int flag) {
    return (errorFlags & flag) > 0;
  }

  private void validate() {
    customErrorResId = 0;
    validateName();
    validatePager();
  }

  private void validateName() {
    if (nameText.getText().length() == 0) {
      nameText.setError(getString(R.string.detail_error_empty_name));
      setError(ERROR_FLAG_NAME);
    } else {
      nameText.setError(null);
      removeError(ERROR_FLAG_NAME);
    }
  }

  private void validatePager() {
    int i = 0;
    for (BaseDetailFragment fragment : pagerAdapter.getRegisteredFragments()) {
      ValidationResponse response = fragment.validate();
      if (!response.isValid()) {
        setError(ERROR_FLAG_PAGER);
        pager.setCurrentItem(i);

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

    SparseArray<WeakReference<Fragment>> mRegisteredFragments =
        new SparseArray<WeakReference<Fragment>>();
    private final String[] mTitles = new String[NUM_FRAGMENTS];

    public ProfilePagerAdapter(FragmentManager fm) {
      super(fm);

      mTitles[0] = getString(R.string.detail_tab_conditions);
      mTitles[1] = getString(R.string.detail_tab_actions);
    }

    @Override public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return ProfileDetailConditionsFragment.create(profile);
        case 1:
          return ProfileDetailActionsFragment.create(profile);
        default:
          Log.e(TAG, "invalid getItem position: " + position);
          return null;
      }
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
      Fragment fragment = (Fragment) super.instantiateItem(container, position);
      mRegisteredFragments.put(position, new WeakReference<Fragment>(fragment));
      return fragment;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      mRegisteredFragments.remove(position);
      super.destroyItem(container, position, object);
    }

    @Override public CharSequence getPageTitle(int position) {
      if (position >= 0 && position < NUM_FRAGMENTS) {
        return mTitles[position];
      }

      Log.e(TAG, "invalid getPageTitle position: " + position);
      return null;
    }

    @Override public int getCount() {
      return NUM_FRAGMENTS;
    }

    public List<BaseDetailFragment> getRegisteredFragments() {
      List<BaseDetailFragment> list = new ArrayList<>(mRegisteredFragments.size());
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
