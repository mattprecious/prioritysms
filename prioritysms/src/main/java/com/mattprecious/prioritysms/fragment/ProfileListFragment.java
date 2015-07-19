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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.adapter.ProfileListAdapter;
import com.mattprecious.prioritysms.db.DbAdapter.SortOrder;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.model.SmsProfile;

public class ProfileListFragment extends SherlockListFragment {
  private static final String STATE_ACTIVATED_POSITION = "activated_position";
  private static final int FREE_PROFILE_LIMIT = 3;

  public interface Callbacks {
    void onItemSelected(BaseProfile profile);
    void onNewProfile(BaseProfile profile);
    boolean isPro();
  }

  private static Callbacks dummyCallbacks = new Callbacks() {
    @Override public void onItemSelected(BaseProfile profile) {}
    @Override public void onNewProfile(BaseProfile profile) {}
    @Override public boolean isPro() {
      return false;
    }
  };

  private Callbacks callbacks = dummyCallbacks;
  private int activatedPosition = ListView.INVALID_POSITION;
  private ProfileListAdapter adapter;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    adapter = new ProfileListAdapter(getActivity());
    setListAdapter(adapter);
  }

  @Override public void onStart() {
    super.onStart();
    EasyTracker.getInstance().setContext(getActivity());
    EasyTracker.getTracker().sendView(getClass().getSimpleName());
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.profile_list_content, container, false);
  }

  @Override public void onResume() {
    super.onResume();
    refreshList();
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
      setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }
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

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_profile_list, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_add_sms:
        if (checkProAndShowDialog()) {
          callbacks.onNewProfile(new SmsProfile());
        }

        return true;
      case R.id.menu_add_phone:
        if (checkProAndShowDialog()) {
          callbacks.onNewProfile(new PhoneProfile());
        }

        return true;
      case R.id.menu_sort:
        adapter.setSortOrder((adapter.getSortOrder() == SortOrder.NAME_ASC) ? SortOrder.NAME_DESC
            : SortOrder.NAME_ASC);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);
    activatedPosition = position;

    callbacks.onItemSelected(adapter.getItem(position));
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (activatedPosition != ListView.INVALID_POSITION) {
      outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
    }
  }

  private boolean checkProAndShowDialog() {
    if (callbacks.isPro() || adapter.getCount() < FREE_PROFILE_LIMIT) {
      return true;
    }

    new ProfileLimitDialogFragment().show(getFragmentManager(), null);

    return false;
  }

  public void refreshList() {
    adapter.notifyDataSetChanged();
  }

  public void clearActivated() {
    setActivatedPosition(ListView.INVALID_POSITION);
  }

  public void setActivateOnItemClick(boolean activateOnItemClick) {
    getListView().setChoiceMode(
        activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
  }

  private void setActivatedPosition(int position) {
    getListView().setItemChecked(position, true);
    activatedPosition = position;
  }
}
