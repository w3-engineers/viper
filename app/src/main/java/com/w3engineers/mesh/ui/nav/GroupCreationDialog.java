package com.w3engineers.mesh.ui.nav;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.w3engineers.ext.viper.R;


public class GroupCreationDialog extends DialogFragment {


    public interface GroupCreationAcceptButtonListener {
        void onAcceptButtonListener(String groupName);
    }

    private GroupCreationAcceptButtonListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View dialogContent = layoutInflater.inflate(R.layout.creation_group_dialog, null);

        final EditText tfGroupName = (EditText) dialogContent.findViewById(R.id.editTextGroupName);

        builder.setView(dialogContent);
        builder.setPositiveButton(getResources().getString(R.string.btn_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = tfGroupName.getText().toString();
                if (listener != null) {
                    if(TextUtils.isEmpty(groupName)){
                        listener.onAcceptButtonListener("meshtest");
                    }else {
                        listener.onAcceptButtonListener(groupName);
                    }
                }
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GroupCreationDialog.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    public void addGroupCreationAcceptListener(GroupCreationAcceptButtonListener listener) {
        this.listener = listener;
    }

}
