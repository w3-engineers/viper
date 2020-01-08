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

public class PeerAdd extends Event implements Parcelable {

    public String peerId;

    public PeerAdd() {

    }

    protected PeerAdd(Parcel in) {
        peerId = in.readString();
    }

    public static final Creator<PeerAdd> CREATOR = new Creator<PeerAdd>() {
        @Override
        public PeerAdd createFromParcel(Parcel in) {
            return new PeerAdd(in);
        }

        @Override
        public PeerAdd[] newArray(int size) {
            return new PeerAdd[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(peerId);
    }
}