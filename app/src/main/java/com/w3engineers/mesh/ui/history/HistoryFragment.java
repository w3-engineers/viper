package com.w3engineers.mesh.ui.history;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.FragmentDatabaseBinding;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.Nearby.UserConnectionCallBack;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.base.ItemClickListener;
import com.w3engineers.mesh.ui.chat.ChatActivity;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.util.AppLog;


import java.util.List;

public class HistoryFragment extends BaseFragment implements ItemClickListener<UserModel>, UserConnectionCallBack {
    private FragmentDatabaseBinding binding;
    private ChattedUserListAdapter chattedUserListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_database, container, false);
        chattedUserListAdapter = new ChattedUserListAdapter(getActivity());
        chattedUserListAdapter.setItemClickListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(chattedUserListAdapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        List<UserModel> list = ChatDataProvider.On().getAllUser();
        chattedUserListAdapter.clear();
        chattedUserListAdapter.addItem(list);
        binding.deviceDetails.setText(AppLog.deviceHistory());

       // ConnectionManager.on().initListener(this);

    }

    @Override
    public void onItemClick(View view, UserModel item) {
        if (item != null) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(UserModel.class.getName(), item);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "User model is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectDisconnect(String userId) {
        binding.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                chattedUserListAdapter.notifyDataSetChanged();
            }
        });
    }
}
