package org.muoncore.extension.introspection;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.MuonEventTransport;
import org.muoncore.MuonExtensionApi;
import org.muoncore.ServiceDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Client for the Muon Introspection extension
 */
public class MuonIntrospectionClient {

    private MuonExtensionApi muon;

    public MuonIntrospectionClient(MuonExtensionApi muon) {
        this.muon = muon;
    }

    public List<ServiceDescriptor> getServiceIdentifers() {
        List<ServiceDescriptor> identifiers = new ArrayList<ServiceDescriptor>();

        for(MuonEventTransport transport: muon.getTransports()) {
            identifiers.addAll(transport.discoverServices());
        }
        return identifiers;
    }

    public List<String> getExtensionsOnService(String identifier) {
        List<String> ret = new ArrayList<String>();

        String identifiers =
                (String) muon.getMuon().get("/muon/inspect/extensions").getResponseEvent().getPayload();

        Object[] data = (Object[]) JSON.parse(identifiers);
        for(Object obj: data) {
            ret.add(obj.toString());
        }
        return ret;
    }

    public List<String> getEventsListenedOnService(String identifier) {
        return null;
    }

    public List<String> getResourcesOnService(String identifier) {
        return null;
    }
}
