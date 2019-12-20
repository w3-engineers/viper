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

import com.w3engineers.models.ConfigurationCommand;

public class ConfigSyncEvent extends Event implements Parcelable {

    public boolean isUpdate;
    public boolean isMeshStartTime;
    public ConfigurationCommand configurationCommand;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public ConfigurationCommand getConfigurationCommand() {
        return configurationCommand;
    }

    public void setConfigurationCommand(ConfigurationCommand configurationCommand) {
        this.configurationCommand = configurationCommand;
    }

    public boolean isMeshStartTime() {
        return isMeshStartTime;
    }

    public void setMeshStartTime(boolean meshStartTime) {
        isMeshStartTime = meshStartTime;
    }

    protected ConfigSyncEvent(Parcel in) {
        isUpdate = in.readByte() != 0;
        isMeshStartTime = in.readByte() != 0;
        configurationCommand = in.readParcelable(ConfigurationCommand.class.getClassLoader());
    }

    public ConfigSyncEvent() {

    }

    public static final Creator<ConfigSyncEvent> CREATOR = new Creator<ConfigSyncEvent>() {
        @Override
        public ConfigSyncEvent createFromParcel(Parcel in) {
            return new ConfigSyncEvent(in);
        }

        @Override
        public ConfigSyncEvent[] newArray(int size) {
            return new ConfigSyncEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (isUpdate ? 1 : 0));
        parcel.writeByte((byte) (isMeshStartTime ? 1 : 0));
        parcel.writeParcelable(configurationCommand, i);
    }
}