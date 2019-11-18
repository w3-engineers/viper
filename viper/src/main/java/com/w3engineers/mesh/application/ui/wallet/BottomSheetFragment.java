package com.w3engineers.mesh.application.ui.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.util.Constant;

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    /**
     * Instance variable
     */
    private String address;
    private Bitmap bitmap;
    private Activity activity;
    public BottomSheetFragment(){}

    @SuppressLint("ValidFragment")
    public BottomSheetFragment(String address) {
        this.address = address;
        String bitmapString = SharedPref.read(Constant.PreferenceKeys.ADDRESS_BITMAP);
        byte [] encodeByte = Base64.decode(bitmapString, Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        ImageView imageView = view.findViewById(R.id.qrImage);
        Button copyButton = view.findViewById(R.id.button_copy_address);
        TextView textView = view.findViewById(R.id.tv_my_address);
        imageView.setImageBitmap(bitmap);
        textView.setText(address);
        copyButton.setOnClickListener(this::onClick);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(activity == null) return;
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(activity, "address copied", Toast.LENGTH_SHORT).show();
    }
}
