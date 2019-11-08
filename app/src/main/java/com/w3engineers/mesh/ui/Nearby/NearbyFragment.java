package com.w3engineers.mesh.ui.Nearby;

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
import com.w3engineers.ext.viper.databinding.FragmentNearbyBinding;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.model.UserModel;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.base.ItemClickListener;
import com.w3engineers.mesh.ui.chat.ChatActivity;
import com.w3engineers.mesh.ui.chat.ChatDataProvider;
import com.w3engineers.mesh.ui.main.UserListAdapter;
import com.w3engineers.mesh.ui.nav.BottomMenuHelper;
import com.w3engineers.mesh.util.ConnectionManager;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.HandlerUtil;
import com.w3engineers.mesh.util.TimeUtil;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NearbyFragment extends BaseFragment implements ItemClickListener<UserModel>, NearbyCallBack {

    private FragmentNearbyBinding binding;
    private UserListAdapter nearbyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nearby, container, false);
        nearbyAdapter = new UserListAdapter(getActivity());
        nearbyAdapter.setItemClickListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(nearbyAdapter);
        binding.myIdTv.setText("My ID :" + SharedPref.read(Constant.KEY_USER_ID));


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<UserModel> list = ConnectionManager.on(getActivity()).getUserList();
                Collections.sort(list);
                nearbyAdapter.clear();
                nearbyAdapter.addItem(list);
                if (nearbyAdapter.getItemCount() > 0) {
                    binding.progressBar.setVisibility(View.GONE);
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }

                if (list.size() > 0) {
                    BottomMenuHelper.showBadge(getActivity(), Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby, String.valueOf(list.size()));
                } else {
                    BottomMenuHelper.removeBadge(Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby);
                }
            }
        });

        ConnectionManager.on(getActivity()).initListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ConnectionManager.on(getActivity()).initListener(null);
    }

    @Override
    public void onItemClick(View view, UserModel item) {
        if (item != null) {
            if (view.getId() == R.id.user_card) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(UserModel.class.getName(), item);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }else {
            Toast.makeText(getActivity(), "User model is null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserFound(UserModel model) {
        HandlerUtil.postForeground(() -> {
            binding.progressBar.setVisibility(View.GONE);
            nearbyAdapter.addItem(model);

            if (nearbyAdapter.getItemCount() > 0) {
                BottomMenuHelper.showBadge(getActivity(), Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby, String.valueOf(nearbyAdapter.getItemCount()));
            } else {
                BottomMenuHelper.removeBadge(Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby);
            }
        });
    }

    @Override
    public void onDisconnectUser(String userId) {
        HandlerUtil.postForeground(() -> {
            nearbyAdapter.removeItem(userId);
            if (getActivity() == null) return;
            if (nearbyAdapter.getItemCount() > 0) {
                BottomMenuHelper.showBadge(getActivity(), Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby, String.valueOf(nearbyAdapter.getItemCount()));
            } else {
                if (getActivity().findViewById(R.id.navigation) != null) {
                    BottomMenuHelper.removeBadge(Objects.requireNonNull(getActivity()).findViewById(R.id.navigation), R.id.navigation_nearby);
                }
            }
        });
    }

    public void updateSentMessageScreen(UserModel userModel) {
        HandlerUtil.postForeground(() -> nearbyAdapter.addItem(userModel));
    }

    public void resetScreen() {
        HandlerUtil.postForeground(() -> {
            for (UserModel model : nearbyAdapter.getItems()) {
                model.setSent(false);
            }
            nearbyAdapter.notifyDataSetChanged();

        });
    }

    public void sendHelloMessage(UserModel model) {
        MessageModel messageModel = new MessageModel();
        messageModel.message = "Hello Bro\n"+ TimeUtil.parseMillisToTime(System.currentTimeMillis());
        messageModel.incoming = false;
        messageModel.friendsId = model.getUserId();
        messageModel.messageId = UUID.randomUUID().toString();
      //  ConnectionManager.on().sendMessage(model.getUserId(), messageModel);
        ChatDataProvider.On().insertMessage(messageModel, model);
    }
}
