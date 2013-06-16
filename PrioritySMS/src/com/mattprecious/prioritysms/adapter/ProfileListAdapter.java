
package com.mattprecious.prioritysms.adapter;

import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.db.DbAdapter;
import com.mattprecious.prioritysms.db.DbAdapter.SortOrder;
import com.mattprecious.prioritysms.model.BaseProfile;

import org.jraf.android.backport.switchwidget.Switch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.List;

import static butterknife.Views.findById;

public class ProfileListAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mInflater;

    private DbAdapter mDbAdapter;

    private List<BaseProfile> mData;

    private SortOrder mSortOrder = SortOrder.NAME_ASC;

    public ProfileListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDbAdapter = new DbAdapter(context);
        refreshData();
    }

    public SortOrder getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        mSortOrder = sortOrder;
        notifyDataSetChanged();
    }

    private void refreshData() {
        mData = mDbAdapter.getProfiles(mSortOrder);
    }

    @Override
    public void notifyDataSetChanged() {
        refreshData();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public BaseProfile getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseProfile profile = getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.profile_list_item, null);
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

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseProfile profile = (BaseProfile) buttonView.getTag();
            profile.setEnabled(isChecked);
            profile.save(mContext);
        }
    };

}
