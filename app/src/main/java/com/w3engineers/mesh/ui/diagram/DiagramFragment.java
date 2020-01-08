package com.w3engineers.mesh.ui.diagram;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.viper.databinding.FragmentDiagramBinding;
import com.w3engineers.mesh.ui.base.BaseFragment;

/*
 *  ****************************************************************************
 *  * Created by : Md Tariqul Islam on 8/29/2019 at 12:59 PM.
 *  * Email : tariqul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md Tariqul Islam on 8/29/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */


public class DiagramFragment extends BaseFragment {

/*    private FragmentDiagramBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_diagram, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onNewLayout(ConstraintLayout newLayout) {
        mBinding.parentLayout.removeAllViews();
        if (DiagramUtil.on(getContext()).getLatestView().getParent() != null) {
            ((ViewGroup) DiagramUtil.on(getContext()).getLatestView().getParent()).removeView(DiagramUtil.on(getContext()).getLatestView());
        }
        mBinding.parentLayout.addView(newLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        DiagramUtil.on(getContext()).setRenewCallback(this);
        if (DiagramUtil.on(getContext()).getLatestView() != null) {
            mBinding.parentLayout.removeAllViews();
            if (DiagramUtil.on(getContext()).getLatestView().getParent() != null) {
                ((ViewGroup) DiagramUtil.on(getContext()).getLatestView().getParent()).removeView(DiagramUtil.on(getContext()).getLatestView());
            }
            mBinding.parentLayout.addView(DiagramUtil.on(getContext()).getLatestView());
        }
    }*/
}
