
package com.mattprecious.prioritysms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.base.Strings;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment;
import com.mattprecious.prioritysms.model.BaseProfile;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ProfileDetailActivity extends SherlockFragmentActivity implements
        ProfileDetailFragment.Callbacks {
    public static final int RESULT_DELETED = 10;

    private String emptyTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            BaseProfile profile = getIntent().getParcelableExtra(
                    ProfileDetailFragment.EXTRA_PROFILE);

            emptyTitle = getString(profile.isNew() ? R.string.detail_empty_title_new
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
                NavUtils.navigateUpTo(this, new Intent(this, ProfileListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNameUpdated(String name) {
        setTitle(name);
    }

    private void setTitle(String title) {
        getSupportActionBar().setTitle(Strings.isNullOrEmpty(title) ? emptyTitle : title);
    }

    @Override
    public void onDiscard() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onDelete() {
        setResult(RESULT_DELETED);
        finish();
    }

    @Override
    public void onSave() {
        setResult(RESULT_OK);
        finish();
    }
}
