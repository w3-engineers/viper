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

public class PeerRemoved extends Event implements Parcelable {

    public String peerId;

    public PeerRemoved() {

    }

    protected PeerRemoved(Parcel in) {
        peerId = in.readString();
    }

    public static final Creator<PeerRemoved> CREATOR = new Creator<PeerRemoved>() {
        @Override
        public PeerRemoved createFromParcel(Parcel in) {
            return new PeerRemoved(in);
        }

        @Override
        public PeerRemoved[] newArray(int size) {
            return new PeerRemoved[size];
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