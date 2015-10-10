package io.muoncore;

import java.net.URI;
import java.util.List;

public class ServiceDescriptor {

    private String identifier;
    private List<String> tags;
    private List<String> codecs;
    private List<URI> connectionUrls;

    public ServiceDescriptor(String identifier,
                             List<String> tags,
                             List<String> codecs,
                             List<URI> streamConnectionUrls) {
        this.identifier = identifier;
        this.tags = tags;
        this.codecs = codecs;
        this.connectionUrls = streamConnectionUrls;
    }

    public List<String> getCodecs() {
        return codecs;
    }
    public List<String> getTags() {
        return tags;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<URI> getConnectionUrls() {
        return connectionUrls;
    }
}
