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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.model.LogicMethod;

public class SmsMethodDialogFragment extends BaseSupportDialogFragment {
  public static final String EXTRA_CURRENT_VALUE = "current_value";

  private static final LogicMethod[] methods = new LogicMethod[] {
      LogicMethod.ALL, LogicMethod.ANY, LogicMethod.ONLY,
  };

  private static final int[] methodDescriptionResIds = new int[] {
      R.string.keywords_method_description_all, R.string.keywords_method_description_any,
      R.string.keywords_method_description_only,
  };

  public static SmsMethodDialogFragment create(LogicMethod currentValue) {
    Bundle args = new Bundle();
    args.putInt(EXTRA_CURRENT_VALUE, currentValue.ordinal());

    SmsMethodDialogFragment fragment = new SmsMethodDialogFragment();
    fragment.setArguments(args);

    return fragment;
  }

  private String[] methodDescriptions = new String[methodDescriptionResIds.length];

  private Callbacks callbacks;
  private LogicMethod currentMethod;

  public void setCallbacks(Callbacks callbacks) {
    this.callbacks = callbacks;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (callbacks == null) {
      throw new IllegalStateException("must call setCallbacks() before showing dialog");
    }

    Bundle args = getArguments();
    if (args == null || !args.containsKey(EXTRA_CURRENT_VALUE)) {
      throw new IllegalArgumentException(
          String.format("must provide %s as an extra", EXTRA_CURRENT_VALUE));
    }

    for (int i = 0; i < methodDescriptionResIds.length; i++) {
      methodDescriptions[i] = getString(methodDescriptionResIds[i]);
    }

    currentMethod = LogicMethod.values()[args.getInt(EXTRA_CURRENT_VALUE)];
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    int currentIndex = 0;
    for (LogicMethod method : methods) {
      if (currentMethod == method) {
        break;
      }

      currentIndex++;
    }

    builder.setTitle(R.string.keywords_method_title);
    builder.setSingleChoiceItems(methodDescriptions, currentIndex,
        new DialogInterface.OnClickListener() {
          @Override public void onClick(@NonNull DialogInterface dialog, int which) {
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

            callbacks.onSelected(method);
            dialog.dismiss();
          }
        }
    );

    return builder.create();
  }

  public interface Callbacks {
    void onSelected(LogicMethod method);
  }
}
