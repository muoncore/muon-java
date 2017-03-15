package io.muoncore;

import lombok.Getter;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceDescriptor {

    @Getter
    private String identifier;
    @Getter
    private List<String> tags;
    private List<String> codecs;
    @Getter
    private Collection<String> capabilities;
    @Getter
    private List<InstanceDescriptor> instanceDescriptors;

    public ServiceDescriptor(String identifier,
                             List<String> tags,
                             List<String> codecs,
                             Collection<String> capabilities, List<InstanceDescriptor> instanceDescriptors) {
        assert identifier != null;
        this.identifier = identifier;
        this.tags = tags;
        this.codecs = codecs;
        this.capabilities = capabilities;
        this.instanceDescriptors = instanceDescriptors;
    }

    public List<String> getSchemes() {
      return instanceDescriptors.stream()
        .map(InstanceDescriptor::getConnectionUrls)
        .flatMap(Collection::stream)
        .map(URI::getScheme)
        .collect(Collectors.toList());
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
}
