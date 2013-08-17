
package com.mattprecious.prioritysms.activity;

import com.google.common.base.Strings;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment;
import com.mattprecious.prioritysms.model.BaseProfile;

import android.content.Intent;
import android.os.Bundle;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ProfileDetailActivity extends BaseActivity implements
        ProfileDetailFragment.Callbacks {

    public static final int RESULT_DELETED = 10;

    private String mEmptyTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            BaseProfile profile = getIntent().getParcelableExtra(
                    ProfileDetailFragment.EXTRA_PROFILE);

            mEmptyTitle = getString(profile.isNew() ? R.string.detail_empty_title_new
                    : R.string.detail_empty_title_edit);

            setTitle(profile.getName());

            Bundle arguments = new Bundle();
            arguments.putParcelable(ProfileDetailFragment.EXTRA_PROFILE, profile);
            ProfileDetailFragment fragment = new ProfileDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.profile_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.clearCroutonsForActivity(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onDiscard();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNameUpdated(String name) {
        setTitle(name);
    }

    private void setTitle(String title) {
        getSupportActionBar().setTitle(Strings.isNullOrEmpty(title) ? mEmptyTitle : title);
    }

    @Override
    public void onDiscard() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onDelete(BaseProfile profile) {
        Intent data = new Intent();
        data.putExtra(ProfileDetailFragment.EXTRA_PROFILE, profile);
        setResult(RESULT_DELETED, data);
        finish();
    }

    @Override
    public void onSave() {
        setResult(RESULT_OK);
        finish();
    }
}
