package org.muoncore.extension.introspection;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.*;
import org.muoncore.transports.MuonResourceEvent;

import java.util.ArrayList;
import java.util.List;

public class IntrospectionExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {

        muonApi.getMuon().onGet("/muon/inspect/extensions",
                "A list of the installed and active library extension on this service",
                new MuonService.MuonGet() {
                    @Override
                    public Object onQuery(MuonResourceEvent queryEvent) {
                        List<String> extensions = new ArrayList<String>();

                        for (MuonExtension extension : muonApi.getExtensions()) {
                            extensions.add(extension.getName());
                        }

                        return JSON.toString(extensions);
                    }
                });

        //get events listened for on this instance
        muonApi.getMuon().onGet("/muon/inspect/events",
                "A list of the events being listened for by this service",
                new MuonService.MuonGet() {
                    @Override
                    public Object onQuery(MuonResourceEvent queryEvent) {
                        return JSON.toString(muonApi.getEvents());
                    }
                });

        //get resources (including docs, verb etc) available on this service
        muonApi.getMuon().onGet("/muon/inspect/resources",
                "A list of the resources available on this service",
                new MuonService.MuonGet() {
                    @Override
                    public Object onQuery(MuonResourceEvent queryEvent) {
                        //todo, better format
                        return JSON.toString(muonApi.getResources());
                    }
                });


    }

    @Override
    public String getName() {
        return "introspection/0.1";
    }
}
