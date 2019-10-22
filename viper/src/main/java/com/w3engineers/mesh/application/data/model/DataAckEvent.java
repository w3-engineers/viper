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

public class DataAckEvent extends Event implements Parcelable {

    public String dataId;
    public int status;

    public DataAckEvent() {

    }

    protected DataAckEvent(Parcel in) {
        dataId = in.readString();
        status = in.readInt();
    }

    public static final Creator<DataAckEvent> CREATOR = new Creator<DataAckEvent>() {
        @Override
        public DataAckEvent createFromParcel(Parcel in) {
            return new DataAckEvent(in);
        }

        @Override
        public DataAckEvent[] newArray(int size) {
            return new DataAckEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(dataId);
        parcel.writeInt(status);
    }
}
