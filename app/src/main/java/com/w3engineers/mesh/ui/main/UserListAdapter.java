package com.w3engineers.mesh.ui.main;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ItemDiscoveredUserBinding;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.base.BaseAdapter;
import com.w3engineers.mesh.ui.base.BaseViewHolder;
import com.w3engineers.mesh.util.ConnectionManager;

import java.util.List;

/**
 * * ============================================================================
 * * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * * Unauthorized copying of this file, via any medium is strictly prohibited
 * * Proprietary and confidential
 * * ----------------------------------------------------------------------------
 * * Created by: Sikder Faysal Ahmed on [15-Jan-2019 at 5:14 PM].
 * * Email: sikderfaysal@w3engineers.com
 * * ----------------------------------------------------------------------------
 * * Project: meshrnd.
 * * Code Responsibility: <Purpose of code>
 * * ----------------------------------------------------------------------------
 * * Edited by :
 * * --> <First Editor> on [15-Jan-2019 at 5:14 PM].
 * * --> <Second Editor> on [15-Jan-2019 at 5:14 PM].
 * * ----------------------------------------------------------------------------
 * * Reviewed by :
 * * --> <First Reviewer> on [15-Jan-2019 at 5:14 PM].
 * * --> <Second Reviewer> on [15-Jan-2019 at 5:14 PM].
 * * ============================================================================
 **/
public class UserListAdapter extends BaseAdapter<UserModel> {


    private Context mContext;

    private ConnectionManager connectionManager;

    public UserListAdapter(Context context) {
        this.mContext = context;
        connectionManager = ConnectionManager.on(context);
    }

    @Override
    public boolean isEqual(UserModel left, UserModel right) {
        if (left.getUserId() == null || right.getUserId() == null) {
            return false;
        }
        if (left.getUserId().equals(right.getUserId())) {
            return true;
        }

        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(inflate(parent, R.layout.item_discovered_user));
    }

    public void removeItem(String userId) {
        List<UserModel> userModelList = getItems();

        for (UserModel item : userModelList) {
            if (item == null || TextUtils.isEmpty(item.getUserId() ))
                continue;

            if (item.getUserId().equals(userId)) {
                removeItem(item);
                return;
            }
        }
    }

    public class UserViewHolder extends BaseViewHolder<UserModel> {

        public UserViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(UserModel item, ViewDataBinding viewDataBinding) {
            ItemDiscoveredUserBinding binding = (ItemDiscoveredUserBinding) viewDataBinding;
            binding.userCard.setOnClickListener(this);

            if (!TextUtils.isEmpty(item.getUserName())) {
                binding.userName.setText(item.getUserName());
            } else {
                binding.userName.setText("Anonymous");
            }
            binding.textViewTime.setText(item.getUserId());
            String connectionType = connectionManager.getConnectionType(item.getUserId());

            binding.userLinkType.setText(connectionType);

            if (item.isSent()) {
                binding.userCard.setBackgroundColor(mContext.getResources().getColor(R.color.colorBackgroundDark));
            } else {
                binding.userCard.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            }
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }
}
