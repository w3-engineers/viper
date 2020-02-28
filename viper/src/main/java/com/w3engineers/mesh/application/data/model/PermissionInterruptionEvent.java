package com.w3engineers.mesh.application.data.model;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class PermissionInterruptionEvent extends Event implements Parcelable {

    public int hardwareState;
    public List<String> permissions;

    public PermissionInterruptionEvent() {

    }

    protected PermissionInterruptionEvent(Parcel in) {
        hardwareState = in.readInt();
        permissions = in.createStringArrayList();
    }

    public static final Creator<PermissionInterruptionEvent> CREATOR = new Creator<PermissionInterruptionEvent>() {
        @Override
        public PermissionInterruptionEvent createFromParcel(Parcel in) {
            return new PermissionInterruptionEvent(in);
        }

        @Override
        public PermissionInterruptionEvent[] newArray(int size) {
            return new PermissionInterruptionEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(hardwareState);
        parcel.writeStringList(permissions);
    }
}