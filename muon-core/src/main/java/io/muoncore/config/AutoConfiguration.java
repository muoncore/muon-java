package io.muoncore.config;

import java.util.ArrayList;
import java.util.List;

public class AutoConfiguration {
    private String discoveryUrl;
    private String serviceName;
    private List<String> tags = new ArrayList<>();
    private String aesEncryptionKey;

    public String getAesEncryptionKey() {
        return aesEncryptionKey;
    }

    public void setAesEncryptionKey(String aesEncryptionKey) {
        this.aesEncryptionKey = aesEncryptionKey;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDiscoveryUrl() {
        return discoveryUrl;
    }

    public void setDiscoveryUrl(String discoveryUrl) {
        this.discoveryUrl = discoveryUrl;
    }
}
