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

public class SellerRemoved extends Event implements Parcelable {

    public String sellerId;

    public SellerRemoved() {

    }

    protected SellerRemoved(Parcel in) {
        sellerId = in.readString();
    }

    public static final Creator<SellerRemoved> CREATOR = new Creator<SellerRemoved>() {
        @Override
        public SellerRemoved createFromParcel(Parcel in) {
            return new SellerRemoved(in);
        }

        @Override
        public SellerRemoved[] newArray(int size) {
            return new SellerRemoved[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sellerId);
    }
}