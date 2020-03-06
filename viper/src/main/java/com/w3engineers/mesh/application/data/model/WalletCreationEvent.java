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

public class WalletCreationEvent extends Event implements Parcelable {

    public boolean successStatus;
    public String nodeId;
    public String statusMessage;

    public WalletCreationEvent() {

    }

    protected WalletCreationEvent(Parcel in) {
        successStatus = in.readByte() != 0;
        nodeId = in.readString();
        statusMessage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (successStatus ? 1 : 0));
        dest.writeString(nodeId);
        dest.writeString(statusMessage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WalletCreationEvent> CREATOR = new Creator<WalletCreationEvent>() {
        @Override
        public WalletCreationEvent createFromParcel(Parcel in) {
            return new WalletCreationEvent(in);
        }

        @Override
        public WalletCreationEvent[] newArray(int size) {
            return new WalletCreationEvent[size];
        }
    };
}
