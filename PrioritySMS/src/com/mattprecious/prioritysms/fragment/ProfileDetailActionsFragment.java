
package com.mattprecious.prioritysms.fragment;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import butterknife.InjectView;
import butterknife.Views;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.fragment.ProfileDetailFragment.BaseDetailFragment;
import com.mattprecious.prioritysms.model.BaseProfile;

public class ProfileDetailActionsFragment extends BaseDetailFragment {
    public static final String EXTRA_PROFILE = "profile";

    public static ProfileDetailActionsFragment create(BaseProfile profile) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_PROFILE, profile);

        ProfileDetailActionsFragment fragment = new ProfileDetailActionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int REQUEST_CODE_RINGTONE_PICKER = 1;

    private BaseProfile mProfile;

    @InjectView(R.id.action_sound)
    Button mSoundButton;
    @InjectView(R.id.action_override_silent)
    CheckBox mOverrideSilentCheckBox;
    @InjectView(R.id.action_vibrate)
    CheckBox mVibrateCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_PROFILE)) {
            mProfile = args.getParcelable(EXTRA_PROFILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_detail_actions, container, false);
        Views.inject(this, rootView);

        updateRingtone(mProfile.getRingtone());
        mSoundButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) v.getTag());

                startActivityForResult(intent, REQUEST_CODE_RINGTONE_PICKER);
            }
        });

        mOverrideSilentCheckBox.setChecked(mProfile.isOverrideSilent());
        mVibrateCheckBox.setChecked(mProfile.isVibrate());

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RINGTONE_PICKER:
                if (resultCode == Activity.RESULT_OK) {
                    Uri ringtoneUri = data
                            .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    updateRingtone(ringtoneUri);
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    @Override
    public void updateProfile(BaseProfile profile) {
        profile.setRingtone((Uri) mSoundButton.getTag());
        profile.setOverrideSilent(mOverrideSilentCheckBox.isChecked());
        profile.setVibrate(mVibrateCheckBox.isChecked());
    }

    private void updateRingtone(Uri ringtoneUri) {
        String ringtoneName = (ringtoneUri == null) ? getString(R.string.actions_ringtone_silent)
                : RingtoneManager.getRingtone(getActivity(), ringtoneUri).getTitle(getActivity());

        mSoundButton.setText(getString(R.string.actions_ringtone, ringtoneName));
        mSoundButton.setTag(ringtoneUri);
    }
}
