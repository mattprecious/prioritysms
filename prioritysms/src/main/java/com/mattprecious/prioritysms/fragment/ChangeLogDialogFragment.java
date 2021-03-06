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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.mattprecious.prioritysms.preferences.SettingsActivity;

public class ChangeLogDialogFragment extends BaseDialogFragment {

  @NonNull @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return SettingsActivity.buildChangeLogDialog(getActivity());
  }
}
