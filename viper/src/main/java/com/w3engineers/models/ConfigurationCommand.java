
package com.w3engineers.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigurationCommand {

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
    @SerializedName("network")
    @Expose
    private List<Network> network = null;

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

}
