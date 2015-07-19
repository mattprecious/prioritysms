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

package com.mattprecious.prioritysms.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.db.DbAdapter.SortOrder;
import com.mattprecious.prioritysms.model.BaseProfile;
import java.util.List;
import org.jraf.android.backport.switchwidget.Switch;

import static butterknife.ButterKnife.findById;

public class ProfileListAdapter extends BaseAdapter {
  private Context context;
  private LayoutInflater inflater;
  private DbAdapter dbAdapter;
  private List<BaseProfile> data;
  private SortOrder sortOrder = SortOrder.NAME_ASC;

  public ProfileListAdapter(Context context) {
    this.context = context;
    inflater = LayoutInflater.from(context);
    dbAdapter = new DbAdapter(context);
    refreshData();
  }

  public SortOrder getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
    notifyDataSetChanged();
  }

  private void refreshData() {
    data = dbAdapter.getProfiles(sortOrder);
  }

  @Override public void notifyDataSetChanged() {
    refreshData();
    super.notifyDataSetChanged();
  }

  @Override public int getCount() {
    return data.size();
  }

  @Override public BaseProfile getItem(int position) {
    return data.get(position);
  }

  @Override public long getItemId(int position) {
    return data.get(position).getId();
  }

  @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    BaseProfile profile = getItem(position);

    if (convertView == null) {
      convertView = inflater.inflate(R.layout.profile_list_item, null);
    }

    TextView nameView = findById(convertView, R.id.profile_name);
    nameView.setText(profile.getName());

    Switch enabledSwitch = findById(convertView, R.id.profile_switch);
    enabledSwitch.setChecked(profile.isEnabled());
    enabledSwitch.setTag(profile);
    enabledSwitch.setOnCheckedChangeListener(switchListener);

    return convertView;
  }

  private OnCheckedChangeListener switchListener = new OnCheckedChangeListener() {

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      BaseProfile profile = (BaseProfile) buttonView.getTag();
      profile.setEnabled(isChecked);
      profile.save(context);
    }
  };
}
