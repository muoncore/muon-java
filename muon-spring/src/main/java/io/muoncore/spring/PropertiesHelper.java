package io.muoncore.spring;

import org.springframework.core.env.*;

import java.util.Properties;

public class PropertiesHelper {

    public static Properties populateConnectionProperties(Environment environment, String muonPrefix) {
        final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        Properties properties = new Properties();
        for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                final EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                for (String name : enumerablePropertySource.getPropertyNames()) {
                    if (isMuonTransportProperty(name, muonPrefix)) {
                        properties.put(name.replace(muonPrefix, ""), enumerablePropertySource.getProperty(name));
                    }
                }
            }
        }
        return properties;
    }
    private static boolean isMuonTransportProperty(String key, String muonPrefix) {
        return key.startsWith(muonPrefix);
    }

}
