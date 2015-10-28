package io.muoncore.spring;

public class MuonConfigurationHolder {
    private String serviceName;
    private String[] tags;
    private String discoveryUrl;

    public MuonConfigurationHolder(String serviceName, String[] tags, String discoveryUrl) {
        this.serviceName = serviceName;
        this.tags = tags;
        this.discoveryUrl = discoveryUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String[] getTags() {
        return tags;
    }

    public String getDiscoveryUrl() {
        return discoveryUrl;
    }

}