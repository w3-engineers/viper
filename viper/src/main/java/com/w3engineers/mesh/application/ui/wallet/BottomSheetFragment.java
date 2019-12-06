package com.w3engineers.mesh.application.ui.wallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.wallet.WalletService;
import com.w3engineers.mesh.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.hdodenhof.circleimageview.CircleImageView;
import lib.folderpicker.FolderPicker;

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    /**
     * Instance variable
     */
    private String address;
    private Bitmap bitmap;
    private Activity activity;
    private final int FOLDER_CHOOSE_ACTION = 100;
    private ProgressDialog dialog;
    private byte[] picture;

    public BottomSheetFragment() {
    }

    @SuppressLint("ValidFragment")
    public BottomSheetFragment(String address, byte[] picture) {
        this.address = address;
        this.picture = picture;
        String bitmapString = SharedPref.read(Constant.PreferenceKeys.ADDRESS_BITMAP);
        byte[] encodeByte = Base64.decode(bitmapString, Base64.DEFAULT);
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
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        ImageView qrImageView = view.findViewById(R.id.qrImage);
        ImageView copyImageView = view.findViewById(R.id.copy_image_view);
        Button copyButton = view.findViewById(R.id.button_export_wallet);
        TextView textView = view.findViewById(R.id.tv_my_address);
        CircleImageView userImageView = view.findViewById(R.id.user_image);
        if(picture != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            userImageView.setImageBitmap(bmp);
        }

        qrImageView.setImageBitmap(bitmap);
        textView.setText(address);
        copyButton.setOnClickListener(this::onClick);
        copyImageView.setOnClickListener(this::onClick);
        return view;
    }

    private void copyAddress() {
        if (activity == null) return;
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(address, address);
        clipboard.setPrimaryClip(clip);
        Toaster.showShort("Copied");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_export_wallet) {
            Intent intent = new Intent(activity, FolderPicker.class);
            startActivityForResult(intent, FOLDER_CHOOSE_ACTION);
        }else if (v.getId() == R.id.copy_image_view){
            copyAddress();
        }
    }


    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toaster.showShort("Please install a File Manager.");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FOLDER_CHOOSE_ACTION && resultCode == Activity.RESULT_OK) {
            String folderLocation = data.getExtras().getString("data");
            //Log.i( "folderLocation", folderLocation );
            Log.e("Choose_dir", "Dir =" + folderLocation);
            String walletPath = WalletService.getInstance(activity).getWalletFilePath();
            Log.e("Choose_dir", "walletPath =" + walletPath);
            showProgress(true);
            copyFileOrDirectory(walletPath, folderLocation);
        }

    }

    private void showProgress(boolean isNeeded) {
        if (isNeeded) {
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Copying please wait...");
            dialog.show();
        } else {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }


    private void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            showProgress(false);
            e.printStackTrace();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            showProgress(false);
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
