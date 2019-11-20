package com.w3engineers.mesh.application.ui.meshlog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.model.MeshLogModel;
import com.w3engineers.mesh.application.ui.base.BaseAdapter;
import com.w3engineers.mesh.application.ui.base.BaseViewHolder;
import com.w3engineers.mesh.databinding.ItemMeshLogDetailsBinding;

import java.util.ArrayList;
import java.util.List;


public class MeshLogAdapter extends BaseAdapter<MeshLogModel> implements Filterable {

    private Context mContext;
    private List<MeshLogModel> mBackupList;
    private String advanceSearchText;
    private List<Integer> matchedPosition;

    public MeshLogAdapter(Context mContext) {
        this.mContext = mContext;
        mBackupList = new ArrayList<>();
        matchedPosition = new ArrayList<>();
    }

    @Override
    public boolean isEqual(MeshLogModel left, MeshLogModel right) {
        return false;
    }

    @Override
    public BaseViewHolder newViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMeshLogDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_mesh_log_details, parent, false);
        return new MeshLogVH(binding);
    }


    @Override
    public int addItem(MeshLogModel item) {
        mBackupList.add(item);
        return super.addItem(item);
    }


    @Override
    public void addItemToPosition(MeshLogModel item, int position) {
        mBackupList.add(position, item);
        super.addItemToPosition(item, position);
    }

    @Override
    public void addItem(List<MeshLogModel> items) {
        super.addItem(items);
        if (mBackupList != null) {
            mBackupList.clear();
            mBackupList.addAll(items);
        }
    }

    @Override
    public void clear() {
        super.clear();
        if (mBackupList != null) {
            mBackupList.clear();
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                if (constraint.toString().isEmpty()) {
                    getItems().clear();
                    getItems().addAll(mBackupList);
                } else {
                    getItems().clear();
                    for (MeshLogModel model : mBackupList) {
                        if (model.getLog().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            getItems().add(model);
                        }
                    }
                }
                final FilterResults results = new FilterResults();
                results.values = getItems();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
    }

    public void advanceSearch(String text) {
        matchedPosition.clear();
        advanceSearchText = text;
        calculateSearchItemPosition();
    }

    public List<Integer> getMatchedPosition() {
        return matchedPosition;
    }

    private void calculateSearchItemPosition() {
        for (int i = 0; i < getItemCount(); i++) {
            try {
                if (getItem(i) != null) {
                    int startPos = getItem(i).getLog().toLowerCase().indexOf(advanceSearchText.toLowerCase());
                    // int endPos = startPos + advanceSearchText.length();
                    if (startPos != -1) {
                        matchedPosition.add(i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class MeshLogVH extends BaseViewHolder<MeshLogModel> {
        ItemMeshLogDetailsBinding binding;

        MeshLogVH(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
            binding = (ItemMeshLogDetailsBinding) viewDataBinding;
        }

        @Override
        public void bind(MeshLogModel item, ViewDataBinding viewDataBinding) {
            if (item == null) return;

            if (item.getType() == 1) {
                binding.textViewLog.setTextColor(mContext.getResources().getColor(R.color.info_color));
            } else if (item.getType() == 2) {
                binding.textViewLog.setTextColor(mContext.getResources().getColor(R.color.warning_color));

            } else if (item.getType() == 3) {
                binding.textViewLog.setTextColor(mContext.getResources().getColor(R.color.error_color));
            } else {
                binding.textViewLog.setTextColor(mContext.getResources().getColor(R.color.colorGradientStart));
            }

            if (TextUtils.isEmpty(advanceSearchText)) {
                binding.textViewLog.setText(item.getLog());
            } else {
                int startPos = item.getLog().toLowerCase().indexOf(advanceSearchText.toLowerCase());
                int endPos = startPos + advanceSearchText.length();

                if (startPos != -1) // This should always be true, just a sanity check
                {
                    Spannable spannable = new SpannableString(item.getLog());
                    ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.BLACK});
                    TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

                    spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    binding.textViewLog.setText(spannable);

                } else {
                    binding.textViewLog.setText(item.getLog());
                }

            }
        }

        @Override
        public void onClick(View v) {

        }
    }
}
