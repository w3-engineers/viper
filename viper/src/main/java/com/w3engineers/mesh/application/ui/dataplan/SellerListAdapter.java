package com.w3engineers.mesh.application.ui.dataplan;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.strom.application.ui.base.BaseAdapter;


import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.databinding.ItemDataSellerBinding;


public class SellerListAdapter extends BaseAdapter<Seller> {

    private final int NEW_SELLER = 1, ONLINE_SELLER = 2, OFFLINE_SELLER = 3, SELLERS = 4;
    private View.OnClickListener clickListener;

    public SellerListAdapter(View.OnClickListener onClickListener) {
        this.clickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        Seller seller = getItem(position);

        switch (seller.getId()) {
            case "1":
                return NEW_SELLER;

            case "2":
                return ONLINE_SELLER;

            case "3":
                return OFFLINE_SELLER;

            default:
                return SELLERS;
        }
    }

    @Override
    public boolean isEqual(Seller left, Seller right) {
        return !TextUtils.isEmpty(left.getId())
                && !TextUtils.isEmpty(right.getId())
                && left.getId().equals(right.getId());
    }

    @Override
    public BaseAdapterViewHolder<Seller> newViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case NEW_SELLER:
                return new TagViewModel(inflate(parent, R.layout.item_label_new_seller));
            case ONLINE_SELLER:
                return new TagViewModel(inflate(parent, R.layout.item_label_online_seller));
            case OFFLINE_SELLER:
                return new TagViewModel(inflate(parent, R.layout.item_label_offline_seller));
            case SELLERS:
                return new SellerViewModel(inflate(parent, R.layout.item_data_seller));
        }

        return null;
    }

    private class TagViewModel extends BaseAdapterViewHolder<Seller> {

        TagViewModel(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(@NonNull Seller item) {

        }
    }

    private class SellerViewModel extends BaseAdapterViewHolder<Seller> {
        private ItemDataSellerBinding itemDataSellerBinding;

        SellerViewModel(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
            this.itemDataSellerBinding = (ItemDataSellerBinding) viewDataBinding;
        }

        @Override
        public void bind(@NonNull Seller seller) {

            Context context = itemDataSellerBinding.userName.getContext();
            itemDataSellerBinding.userName.setText(seller.getName());

            String usedDataInfo = String.format(context.getResources().getString(R.string.used_s), " " + seller.getUsedData())
                    + " " + String.format(context.getResources().getString(R.string.total_mb) , " " + seller.getPurchasedData());

            itemDataSellerBinding.userUseAmount.setText(usedDataInfo);

            itemDataSellerBinding.status.setText(seller.getBtnText());
            itemDataSellerBinding.status.setEnabled(seller.isBtnEnabled());



            itemDataSellerBinding.status.setBackground(seller.isBtnEnabled() ? ContextCompat.getDrawable(context, R.drawable.ractangular_green_small) : ContextCompat.getDrawable(context, R.drawable.rectangular_gray_small));
            itemDataSellerBinding.status.setTextColor(seller.isBtnEnabled() ? context.getResources().getColor(R.color.colorGradientPrimary) : context.getResources().getColor(R.color.grey_text) );

            int padding =  itemDataSellerBinding.status.getPaddingTop();
            itemDataSellerBinding.status.setPadding(padding, padding, padding, padding);

            itemDataSellerBinding.status.setTag(seller);
            itemDataSellerBinding.status.setOnClickListener(clickListener);
        }
    }
}
