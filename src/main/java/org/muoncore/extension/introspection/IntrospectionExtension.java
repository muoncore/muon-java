package org.muoncore.extension.introspection;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.MuonService;
import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;

import java.util.ArrayList;
import java.util.List;

public class IntrospectionExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {

        muonApi.getMuon().resource("/muon/inspect/extensions",
                "A list of the installed and active library extension on this service",
                new MuonService.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                List<String> extensions = new ArrayList<String>();

                for(MuonExtension extension: muonApi.getExtensions()) {
                    extensions.add(extension.getName());
                }

                return JSON.toString(extensions);
            }
        });


        //get events listened for on this service

        //get resources (including docs, verb etc) available on this service

    }

    @Override
    public String getName() {
        return "introspection/1.0";
    }
}
