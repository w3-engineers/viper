
package com.w3engineers.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigurationCommand implements Parcelable {

    @SerializedName("config_version_name")
    @Expose
    private String configVersionName;
    @SerializedName("config_version_code")
    @Expose
    private Integer configVersionCode;
    @SerializedName("token_per_mb")
    @Expose
    private Float tokenPerMb;
    @SerializedName("default_network_type")
    @Expose
    private Integer defaultNetworkType;

    @SerializedName("token_guide_version")
    @Expose
    private Integer tokenGuideVersion;

    @SerializedName("GIFT_DONATE_LINK")
    @Expose
    private String giftDonateLink;

    @SerializedName("max_point_for_rmesh")
    @Expose
    private long maxPointForRmesh;

    @SerializedName("rmesh_per_token")
    @Expose
    private float rmeshPerToken;

    @SerializedName("network")
    @Expose
    private List<Network> network = null;

    protected ConfigurationCommand(Parcel in) {
        configVersionName = in.readString();
        if (in.readByte() == 0) {
            configVersionCode = null;
        } else {
            configVersionCode = in.readInt();
        }
        if (in.readByte() == 0) {
            tokenPerMb = null;
        } else {
            tokenPerMb = in.readFloat();
        }
        if (in.readByte() == 0) {
            defaultNetworkType = null;
        } else {
            defaultNetworkType = in.readInt();
        }
        if (in.readByte() == 0) {
            tokenGuideVersion = null;
        } else {
            tokenGuideVersion = in.readInt();
        }

        giftDonateLink = in.readString();
        maxPointForRmesh = in.readLong();
        rmeshPerToken = in.readFloat();

        network = in.createTypedArrayList(Network.CREATOR);
    }

    public static final Creator<ConfigurationCommand> CREATOR = new Creator<ConfigurationCommand>() {
        @Override
        public ConfigurationCommand createFromParcel(Parcel in) {
            return new ConfigurationCommand(in);
        }

        @Override
        public ConfigurationCommand[] newArray(int size) {
            return new ConfigurationCommand[size];
        }
    };

    public String getConfigVersionName() {
        return configVersionName;
    }

    public void setConfigVersionName(String configVersionName) {
        this.configVersionName = configVersionName;
    }

    public Integer getConfigVersionCode() {
        return configVersionCode;
    }

    public void setConfigVersionCode(Integer configVersionCode) {
        this.configVersionCode = configVersionCode;
    }

    public Float getTokenPerMb() {
        return tokenPerMb;
    }

    public void setTokenPerMb(Float tokenPerMb) {
        this.tokenPerMb = tokenPerMb;
    }

    public Integer getDefaultNetworkType() {
        return defaultNetworkType;
    }

    public void setDefaultNetworkType(Integer defaultNetworkType) {
        this.defaultNetworkType = defaultNetworkType;
    }

    public List<Network> getNetwork() {
        return network;
    }

    public void setNetwork(List<Network> network) {
        this.network = network;
    }

    public Integer getTokenGuideVersion() {
        return tokenGuideVersion;
    }

    public void setTokenGuideVersion(Integer tokenGuideVersion) {
        this.tokenGuideVersion = tokenGuideVersion;
    }

    public String getGiftDonateLink() {
        return giftDonateLink;
    }

    public void setGiftDonateLink(String giftDonateLink) {
        this.giftDonateLink = giftDonateLink;
    }

    public long getMaxPointForRmesh() {
        return maxPointForRmesh;
    }

    public void setMaxPointForRmesh(long maxPointForRmesh) {
        this.maxPointForRmesh = maxPointForRmesh;
    }

    public float getRmeshPerToken() {
        return rmeshPerToken;
    }

    public void setRmeshPerToken(float rmeshPerToken) {
        this.rmeshPerToken = rmeshPerToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(configVersionName);
        if (configVersionCode == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(configVersionCode);
        }
        if (tokenPerMb == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(tokenPerMb);
        }
        if (defaultNetworkType == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(defaultNetworkType);
        }
        if (tokenGuideVersion == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(tokenGuideVersion);
        }

        parcel.writeString(giftDonateLink);
        parcel.writeLong(maxPointForRmesh);
        parcel.writeFloat(rmeshPerToken);

        parcel.writeTypedList(network);
    }
}
