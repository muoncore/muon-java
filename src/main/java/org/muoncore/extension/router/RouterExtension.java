package org.muoncore.extension.router;

import org.muoncore.Muon;
import org.muoncore.MuonEvent;
import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;
import org.muoncore.filter.EventFilterChain;

import java.util.List;

public class RouterExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {

        muonApi.getMuon().resource("/muon/router", "Manage the Muon routing table", new Muon.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return getFormattedTable(muonApi.getFilterChains());
            }
        });

        muonApi.getMuon().resource("/muon/router", "Manage the muon routing table", new Muon.MuonPost() {
            @Override
            public Object onCommand(Object postData) {
                muonApi.getFilterChains().add(generateFilter(postData));
                return getFormattedTable(muonApi.getFilterChains());
            }
        });
    }
















    


    public EventFilterChain generateFilter(Object postData) {
           return null;
    }
    public String getFormattedTable(List filters) {
        return "Routing is cool";
    }
}
