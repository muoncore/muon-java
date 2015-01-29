package org.muoncore;

import javax.xml.ws.Service;
import java.net.URI;
import java.util.List;

public interface Discovery {
    ServiceDescriptor getService(URI uri);
    List<ServiceDescriptor> getKnownServices();
    void advertiseLocalService(ServiceDescriptor descriptor);
}
