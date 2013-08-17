
package com.mattprecious.prioritysms.fragment;

import com.google.analytics.tracking.android.EasyTracker;

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
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.adapter.ProfileListAdapter;
import com.mattprecious.prioritysms.db.DbAdapter.SortOrder;
import com.mattprecious.prioritysms.model.BaseProfile;
import com.mattprecious.prioritysms.model.PhoneProfile;
import com.mattprecious.prioritysms.model.SmsProfile;

public class ProfileListFragment extends SherlockListFragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private static final int FREE_PROFILE_LIMIT = 3;

    private Callbacks mCallbacks = sDummyCallbacks;

    private int mActivatedPosition = ListView.INVALID_POSITION;

    private ProfileListAdapter mAdapter;

    public interface Callbacks {

        public void onItemSelected(BaseProfile profile);

        public void onNewProfile(BaseProfile profile);

        public boolean isPro();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(BaseProfile profile) {
        }

        @Override
        public void onNewProfile(BaseProfile profile) {
        }

        @Override
        public boolean isPro() {
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new ProfileListAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().setContext(getActivity());
        EasyTracker.getTracker().sendView(getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_list_content, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_sms:
                if (checkProAndShowDialog()) {
                    mCallbacks.onNewProfile(new SmsProfile());
                }

                return true;
            case R.id.menu_add_phone:
                if (checkProAndShowDialog()) {
                    mCallbacks.onNewProfile(new PhoneProfile());
                }

                return true;
            case R.id.menu_sort:
                mAdapter.setSortOrder((mAdapter.getSortOrder() == SortOrder.NAME_ASC)
                        ? SortOrder.NAME_DESC
                        : SortOrder.NAME_ASC);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mActivatedPosition = position;

        mCallbacks.onItemSelected(mAdapter.getItem(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    private boolean checkProAndShowDialog() {
        if (mCallbacks.isPro() || mAdapter.getCount() < FREE_PROFILE_LIMIT) {
            return true;
        }

        new ProfileLimitDialogFragment().show(getFragmentManager(), null);

        return false;
    }

    public void refreshList() {
        mAdapter.notifyDataSetChanged();
    }

    public void clearActivated() {
        setActivatedPosition(ListView.INVALID_POSITION);
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        getListView().setItemChecked(position, true);
        mActivatedPosition = position;
    }
}
