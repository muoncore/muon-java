package io.muoncore.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AutoConfiguration {
    private String serviceName;
    private List<String> tags = new ArrayList<>();
    private Properties config = new Properties();

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

    public Properties getProperties() {
        return config;
    }

    public long getLongConfig(String name) {
        return Long.parseLong(config.getProperty(name));
    }

    public long getLongConfig(String name, long defaultValue) {
        try {
            return Long.parseLong(config.getProperty(name));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBooleanConfig(String name) {
        return Boolean.parseBoolean(config.getProperty(name));
    }

    public String getStringConfig(String name) {
        return config.getProperty(name);
    }
}
