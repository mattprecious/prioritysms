package com.mattprecious.prioritysms.fragment;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.LogicMethod;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class SmsMethodDialogFragment extends BaseSupportDialogFragment {

    public static final String EXTRA_CURRENT_VALUE = "current_value";

    private static final LogicMethod[] sMethods = new LogicMethod[] {
        LogicMethod.ALL,
        LogicMethod.ANY,
        LogicMethod.ONLY,
    };

    private static final int[] sMethodDescriptionResIds = new int[] {
        R.string.keywords_method_description_all,
        R.string.keywords_method_description_any,
        R.string.keywords_method_description_only,
    };

    public static SmsMethodDialogFragment create(LogicMethod currentValue) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_CURRENT_VALUE, currentValue.ordinal());

        SmsMethodDialogFragment fragment = new SmsMethodDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private String[] mMethodDescriptions = new String[sMethodDescriptionResIds.length];

    private Callbacks mCallbacks;
    private LogicMethod mCurrentMethod;

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCallbacks == null) {
            throw new IllegalStateException("must call setCallbacks() before showing dialog");
        }

        Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_CURRENT_VALUE)) {
            throw new IllegalArgumentException(String.format("must provide %s as an extra",
                    EXTRA_CURRENT_VALUE));
        }

        for (int i = 0; i < sMethodDescriptionResIds.length; i++) {
            mMethodDescriptions[i] = getString(sMethodDescriptionResIds[i]);
        }

        mCurrentMethod = LogicMethod.values()[args.getInt(EXTRA_CURRENT_VALUE)];
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        int currentIndex = 0;
        for (LogicMethod method : sMethods) {
            if (mCurrentMethod == method) {
                break;
            }

            currentIndex++;
        }

        builder.setTitle(R.string.keywords_method_title);
        builder.setSingleChoiceItems(mMethodDescriptions, currentIndex,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogicMethod method = LogicMethod.ANY;
                switch (which) {
                    case 0:
                        method = LogicMethod.ALL;
                        break;
                    case 1:
                        method = LogicMethod.ANY;
                        break;
                    case 2:
                        method = LogicMethod.ONLY;
                        break;
                }

                mCallbacks.onSelected(method);
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    public static interface Callbacks {

        public void onSelected(LogicMethod method);
    }
}
