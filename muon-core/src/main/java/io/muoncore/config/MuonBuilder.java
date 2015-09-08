package io.muoncore.config;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.MuonExtension;
import io.muoncore.config.writers.ConfigFileConfigurationWriter;
import io.muoncore.config.writers.DockerConfigurationWriter;
import io.muoncore.exception.MuonException;
import io.muoncore.transport.resource.MuonResourceTransport;

import java.util.ArrayList;
import java.util.List;

public class MuonBuilder {

    static List<AutoConfigurationWriter> writers = new ArrayList<>();
    static List<ExtensionBuilder> extensions = new ArrayList<>();
    static DiscoveryBuilder discovery;

    private String[] tags = new String[0];
    private String serviceIdentifier;
    private AutoConfiguration config = new AutoConfiguration();

    public MuonBuilder withServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
        return this;
    }

    public MuonBuilder withTags(String...name) {
        this.tags = tags;
        return this;
    }

    public Muon build() {
        writers.forEach(writer -> writer.writeConfiguration(config));

        Muon muon = new Muon(discovery.create(config));
        muon.setServiceIdentifer(serviceIdentifier);
        muon.addTags(tags);

        extensions.forEach(extension -> extension.create(config).extend(muon));

        return muon;
    }

    public interface DiscoveryBuilder {
        Discovery create(AutoConfiguration config);
    }

    public interface ExtensionBuilder {
        MuonExtension create(AutoConfiguration config);
    }

    public static void registerDiscovery(DiscoveryBuilder builder) {
        discovery = builder;
    }
    public static void registerExtension(ExtensionBuilder builder) {
        extensions.add(builder);
    }

    static {
        //for demo purposes, we boot these here. Need to get a cleaner way of either auto detecting, configuring etc.
        try {
            Class.forName("io.muoncore.extension.amqp.discovery.AmqpDiscovery");
            Class.forName("io.muoncore.extension.amqp.AmqpTransportExtension");

            writers.add(new DockerConfigurationWriter());
            writers.add(new ConfigFileConfigurationWriter());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
