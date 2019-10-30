package com.w3engineers.mesh.ui.chat;

/*
 *  ****************************************************************************
 *  * Created by : Md. Azizul Islam on 11/30/2018 at 6:43 PM.
 *  * Email : azizul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md. Azizul Islam on 11/30/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.databinding.ViewDataBinding;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.ItemTextMessageInBinding;
import com.w3engineers.ext.viper.databinding.ItemTextMessageOutBinding;
import com.w3engineers.mesh.model.MessageModel;
import com.w3engineers.mesh.ui.base.BaseAdapter;
import com.w3engineers.mesh.ui.base.BaseViewHolder;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.TimeUtil;



public class ChatAdapter extends BaseAdapter<MessageModel> {
    private final int TEXT_IN = 1;
    private final int TEXT_OUT = 2;

    @Override
    public int getItemViewType(int position) {
        MessageModel item = getItem(position);
        if (item == null)
            return TEXT_OUT;

        if (item.incoming) {
            return TEXT_IN;
        } else {
            return TEXT_OUT;
        }
    }

    @Override
    public boolean isEqual(MessageModel left, MessageModel right) {
        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        switch (viewType) {
            case TEXT_IN:
                baseViewHolder = new TextInHolder(inflate(parent, R.layout.item_text_message_in));
                break;
            case TEXT_OUT:
                baseViewHolder = new TextOutHolder(inflate(parent, R.layout.item_text_message_out));
                break;
        }
        return baseViewHolder;
    }


    private class TextInHolder extends BaseViewHolder<MessageModel> {

        public TextInHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(MessageModel item, ViewDataBinding viewDataBinding) {
            ItemTextMessageInBinding binding = (ItemTextMessageInBinding) viewDataBinding;
            int padding = binding.textViewMessage.getPaddingTop();
            binding.textViewMessage.setPadding(padding, padding, padding, padding);
            binding.setMessage(item);
            binding.textViewDateTime.setText(TimeUtil.parseMillisToTime(item.receiveTime));
        }

        @Override
        public void onClick(View v) {

        }
    }

    private class TextOutHolder extends BaseViewHolder<MessageModel> {
        public TextOutHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
        }

        @Override
        public void bind(MessageModel item, ViewDataBinding viewDataBinding) {
            ItemTextMessageOutBinding binding = (ItemTextMessageOutBinding) viewDataBinding;
            binding.setMessage(item);
            int padding = binding.textViewMessage.getPaddingTop();
            binding.textViewMessage.setPadding(padding, padding, padding, padding);

            if (item.messageStatus == Constant.MessageStatus.FAILED) {
                binding.textViewDateTime.setText("Failed");
            } else if (item.messageStatus == Constant.MessageStatus.RECEIVED) {
                binding.textViewDateTime.setText("Received");
            } else if (item.messageStatus == Constant.MessageStatus.DELIVERED) {
                binding.textViewDateTime.setText("Delivered");
            } else if (item.messageStatus == Constant.MessageStatus.SEND) {
                binding.textViewDateTime.setText("Send");
            } else {
                binding.textViewDateTime.setText("Sending...");
            }
        }

        @Override
        public void onClick(View v) {
        }
    }
}
