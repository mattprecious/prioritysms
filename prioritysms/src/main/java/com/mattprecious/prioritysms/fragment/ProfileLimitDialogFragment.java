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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.mattprecious.prioritysms.R;

public class ProfileLimitDialogFragment extends BaseSupportDialogFragment {

  public interface Callbacks {
    public void onGoProClick();
  }

  private static Callbacks dummyCallbacks = new Callbacks() {
    @Override  public void onGoProClick() {}
  };

  private Callbacks callbacks = dummyCallbacks;

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

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    builder.setTitle(R.string.limit_reached_title);
    builder.setMessage(R.string.limit_reached_message);
    builder.setNegativeButton(R.string.limit_reached_negative,
        new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }
    );

    builder.setPositiveButton(R.string.limit_reached_positive,
        new DialogInterface.OnClickListener() {

          @Override public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            callbacks.onGoProClick();
          }
        }
    );

    return builder.create();
  }
}
