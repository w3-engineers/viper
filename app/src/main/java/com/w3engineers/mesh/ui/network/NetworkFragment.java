package com.w3engineers.mesh.ui.network;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.FragmentNetworkBinding;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.base.ItemClickListener;
import com.w3engineers.mesh.util.Constant;


import java.util.List;

public class NetworkFragment extends BaseFragment  {
    private FragmentNetworkBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_network, container, false);

        //binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // binding.progressBar.setVisibility(ViewUtil.GONE);
        //binding.textTitle.setText(SharedPref.onLinkReceivedFrame(Constants.KEY_USER_NAME) + "'s address");
        binding.userAddress.setText(SharedPref.read(Constant.KEY_USER_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDeviceInfo();
    }


    public void updateDeviceInfo() {
        /*String name = SharedPref.onLinkReceivedFrame(Constants.DEVICE_NAME);
        boolean isMaster = SharedPref.readBoolean(Constant.KEY_P2P_MASTER);
        String group = "Client";
        if (isMaster) {
            group = "Master";
        }
        binding.textName.setText(name + " =:" + group);*/
/*        binding.tvWifiDirect.setText("WIFI link count: " + ConnectionManager.on().getLinkCount(1));
        binding.tvWifMesh.setText("WIFI Mesh link count: " + ConnectionManager.on().getLinkCount(2));
        binding.tvBleDirect.setText("BLE link count: " + ConnectionManager.on().getLinkCount(3));
        binding.tvBleMesh.setText("BLE Mesh link count: " + ConnectionManager.on().getLinkCount(4));
        binding.networkName.setText("Network: " + ConnectionManager.on().getNetworkPrefix());*/

    //    List<String> uiUserlinkList = ConnectionManager.on().getUserIdList();
        StringBuilder userListString = new StringBuilder();
/*        if (uiUserlinkList != null) {
            for (String item : uiUserlinkList) {
                userListString.append("\n").append(item);
            }
        }*/


/*        List<String> wifiUserLinkList = ConnectionManager.on().getWifiLinkIds();
        StringBuilder wifiUserLinkString = new StringBuilder();
        if (wifiUserLinkList != null) {
            for (String item : wifiUserLinkList) {
                wifiUserLinkString.append("\n").append(item);
            }
        }*/

/*
        List<String> wifiMeshUserLinkList = ConnectionManager.on().getWifiMeshLinkIds();
        StringBuilder wifiMeshUserLinkString = new StringBuilder();
        if (wifiMeshUserLinkList != null) {
            for (String item : wifiMeshUserLinkList) {
                wifiMeshUserLinkString.append("\n").append(item);
            }
        }
*/


/*        List<String> bleUserLinkList = ConnectionManager.on().getBleLinkIds();
        StringBuilder bleUserLinkString = new StringBuilder();
        if (bleUserLinkList != null) {
            for (String item : bleUserLinkList) {
                bleUserLinkString.append("\n").append(item);
            }
        }*/


/*        List<String> bleMeshUserLinkList = ConnectionManager.on().getBleMeshLinkIds();
        StringBuilder bleMeshUserLinkString = new StringBuilder();
        if (bleMeshUserLinkList != null) {
            for (String item : bleMeshUserLinkList) {
                bleMeshUserLinkString.append("\n").append(item);
            }
        }*/

       // binding.tvNetworkDetails.setText("UI user IDS: " + "\n" + userListString + "\n \nWifi IDS:\n " + wifiUserLinkString + "\n\nWifi Mesh IDS: \n" + wifiMeshUserLinkString + "\n \nBT. IDS: \n" + bleUserLinkString + "\n\nBT. Mesh IDS: \n" + bleMeshUserLinkString);
    }


}
