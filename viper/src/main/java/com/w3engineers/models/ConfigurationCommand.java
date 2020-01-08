
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
    private int configVersionCode;
    @SerializedName("token_per_mb")
    @Expose
    private float tokenPerMb;
    @SerializedName("default_network_type")
    @Expose
    private int defaultNetworkType;

    @SerializedName("token_guide_version")
    @Expose
    private int tokenGuideVersion;

    @SerializedName("GIFT_DONATE_LINK")
    @Expose
    private String giftDonateLink;

    @SerializedName("max_point_for_rmesh")
    @Expose
    private long maxPointForRmesh;

    @SerializedName("rmesh_per_token")
    @Expose
    private float rmeshPerToken;

    @SerializedName("wallet_rmesh_available")
    @Expose
    private boolean walletRmeshAvailable;

    @SerializedName("rmesh_info_text")
    @Expose
    private String rmeshInfoText;

    @SerializedName("rmesh_owner_address")
    @Expose
    private String rmeshOwnerAddress;

    @SerializedName("main_net_network_type")
    @Expose
    private int mainNetNetworkType;

    @SerializedName("network")
    @Expose
    private List<Network> network = null;

    protected ConfigurationCommand(Parcel in) {
        configVersionName = in.readString();
        configVersionCode = in.readInt();
        tokenPerMb = in.readFloat();
        defaultNetworkType = in.readInt();
        tokenGuideVersion = in.readInt();

        giftDonateLink = in.readString();
        maxPointForRmesh = in.readLong();
        rmeshPerToken = in.readFloat();

        walletRmeshAvailable = in.readByte() != 0;
        rmeshInfoText = in.readString();
        rmeshOwnerAddress = in.readString();
        mainNetNetworkType = in.readInt();

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

    public boolean isWalletRmeshAvailable() {
        return walletRmeshAvailable;
    }

    public void setWalletRmeshAvailable(boolean walletRmeshAvailable) {
        this.walletRmeshAvailable = walletRmeshAvailable;
    }

    public String getRmeshInfoText() {
        return rmeshInfoText;
    }

    public void setRmeshInfoText(String rmeshInfoText) {
        this.rmeshInfoText = rmeshInfoText;
    }

    public String getRmeshOwnerAddress() {
        return rmeshOwnerAddress;
    }

    public void setRmeshOwnerAddress(String rmeshOwnerAddress) {
        this.rmeshOwnerAddress = rmeshOwnerAddress;
    }

    public int getMainNetNetworkType() {
        return mainNetNetworkType;
    }

    public void setMainNetNetworkType(int mainNetNetworkType) {
        this.mainNetNetworkType = mainNetNetworkType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(configVersionName);
        parcel.writeInt(configVersionCode);
        parcel.writeFloat(tokenPerMb);
        parcel.writeInt(defaultNetworkType);
        parcel.writeInt(tokenGuideVersion);

        parcel.writeString(giftDonateLink);
        parcel.writeLong(maxPointForRmesh);
        parcel.writeFloat(rmeshPerToken);

        parcel.writeByte((byte) (walletRmeshAvailable ? 1 : 0));
        parcel.writeString(rmeshInfoText);
        parcel.writeString(rmeshOwnerAddress);
        parcel.writeInt(mainNetNetworkType);

        parcel.writeTypedList(network);
    }
}
