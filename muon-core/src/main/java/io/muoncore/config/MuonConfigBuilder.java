package io.muoncore.config;

import io.muoncore.config.writers.ConfigFileConfigurationWriter;
import io.muoncore.config.writers.DefaultConfigurationWriter;
import io.muoncore.config.writers.DockerLinkConfigurationWriter;
import io.muoncore.config.writers.EnvironmentConfigurationWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuonConfigBuilder {

    private List<AutoConfigurationWriter> writers = new ArrayList<>();

    private String[] tags = new String[0];
    private String serviceIdentifier;

    private MuonConfigBuilder() {
        addWriter(new DefaultConfigurationWriter());
        addWriter(new ConfigFileConfigurationWriter());
        addWriter(new EnvironmentConfigurationWriter());
        addWriter(new DockerLinkConfigurationWriter());
    }

    public static MuonConfigBuilder withServiceIdentifier(String serviceIdentifier) {
        MuonConfigBuilder builder = new MuonConfigBuilder();
        builder.serviceIdentifier = serviceIdentifier;
        return builder;
    }

    public MuonConfigBuilder withTags(String... tags) {
        this.tags = tags;
        return this;
    }

    public AutoConfiguration build() {
        AutoConfiguration config = new AutoConfiguration();
        config.setServiceName(serviceIdentifier);
        config.setTags(Arrays.asList(tags));
        writers.forEach(writer -> writer.writeConfiguration(config));
        return config;
    }

    public MuonConfigBuilder addWriter(AutoConfigurationWriter writer) {
        writers.add(writer);
        return this;
    }
}
