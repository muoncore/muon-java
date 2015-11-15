package io.muoncore.spring;

import io.muoncore.spring.transport.MuonTransportFactoryBeanRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Properties;

public class PropertiesHelper {

    public static Properties populateConnectionProperties(Environment environment, String mounPrefix) {
        final ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        Properties properties = new Properties();
        for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
            if (propertySource instanceof MapPropertySource) {
                ((MapPropertySource) propertySource).getSource().entrySet()
                        .stream()
                        .filter(property -> isMuonTransportProperty(property.getKey(), mounPrefix))
                        .forEach(property -> properties.put(property.getKey().replace(mounPrefix, ""), property.getValue()));
            }
        }
        return properties;
    }
    private static boolean isMuonTransportProperty(String key, String muonPrefix) {
        return key.startsWith(muonPrefix);
    }

}
