package com.mattprecious.prioritysms.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.mattprecious.prioritysms.R;

public class ProfileLimitDialogFragment extends DialogFragment {

    public interface Callbacks {
        public void onGoProClick();
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onGoProClick() {
        }
    };

    private Callbacks mCallbacks = sDummyCallbacks;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.limit_reached_title);
        builder.setMessage(R.string.limit_reached_message);
        builder.setNegativeButton(R.string.limit_reached_negative,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        });

        builder.setPositiveButton(R.string.limit_reached_positive,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mCallbacks.onGoProClick();
                    }
        });

        return builder.create();
    }
}
