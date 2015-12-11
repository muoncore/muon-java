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
                             List<URI> connectionUrls) {
        this.identifier = identifier;
        this.tags = tags;
        this.codecs = codecs;
        this.connectionUrls = connectionUrls;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDescriptor that = (ServiceDescriptor) o;

        return getIdentifier().equals(that.getIdentifier());

    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public String[] getCodecs() {
        return codecs.toArray(new String[0]);
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
