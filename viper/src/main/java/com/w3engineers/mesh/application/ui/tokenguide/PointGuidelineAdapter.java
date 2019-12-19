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
import com.w3engineers.models.PointLink;

public class PointGuidelineAdapter extends BaseAdapter<PointLink> {

    @Override
    public boolean isEqual(PointLink left, PointLink right) {
        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemLinkBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_link, parent, false);
        return new TokenGuidelineVH(binding);
    }

    class TokenGuidelineVH extends BaseViewHolder<PointLink> {
        ItemLinkBinding binding;

        TokenGuidelineVH(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(PointLink item, ViewDataBinding viewDataBinding) {
            binding = (ItemLinkBinding) viewDataBinding;

            binding.textViewLink.setText(item.getHeader());

            setClickListener(binding.getRoot());
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getItem(getAdapterPosition()));
        }
    }
}
