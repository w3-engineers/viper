package com.w3engineers.mesh.application.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ServiceUpdate extends Event implements Parcelable {

    public boolean isNeeded;

    public ServiceUpdate() {

    }

    protected ServiceUpdate(Parcel in) {
        isNeeded = in.readByte() == 1;
    }

    public static final Creator<ServiceUpdate> CREATOR = new Creator<ServiceUpdate>() {
        @Override
        public ServiceUpdate createFromParcel(Parcel in) {
            return new ServiceUpdate(in);
        }

        @Override
        public ServiceUpdate[] newArray(int size) {
            return new ServiceUpdate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isNeeded ? 1 : 0));
    }
}
