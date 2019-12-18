package com.w3engineers.mesh.application.ui.tokenguide;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.ui.base.BaseAdapter;
import com.w3engineers.mesh.application.ui.base.BaseViewHolder;
import com.w3engineers.mesh.databinding.ItemLinkBinding;

public class TokenGuidelineAdapter extends BaseAdapter<String> {

    @Override
    public boolean isEqual(String left, String right) {
        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemLinkBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_link, parent, false);
        return new TokenGuidelineVH(binding);
    }

    class TokenGuidelineVH extends BaseViewHolder<String> {
        ItemLinkBinding binding;

        TokenGuidelineVH(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(String item, ViewDataBinding viewDataBinding) {
            binding = (ItemLinkBinding) viewDataBinding;

            binding.textViewLink.setText(item);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
