package org.muoncore;

import javax.xml.ws.Service;
import java.net.URI;
import java.util.List;

public interface Discovery {
    /**
     * Lookup a remote service via the muon:// url scheme
     */
    ServiceDescriptor getService(URI uri);

    /**
     * Return all of the services that are currently visible by this discovery mechanism
     * @return
     */
    List<ServiceDescriptor> getKnownServices();

    /**
     * Advertise a particular service.
     * If null, then nothing is advertised by the local process
     * @param descriptor
     */
    void advertiseLocalService(ServiceDescriptor descriptor);
}
