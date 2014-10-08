package org.muoncore.extension.router;

import org.muoncore.MuonService;
import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;

public class RouterExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {

        muonApi.getMuon().resource("/muon/router", "Manage the Muon routing table", new MuonService.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                return "";
            }
        });

        muonApi.getMuon().resource("/muon/router", "Manage the muon routing table", new MuonService.MuonPost() {
            @Override
            public Object onCommand(Object postData) {
                //todo, manipulate the filter chains.
//                muonApi.getFilterChains().add(generateFilter(postData));
                return "";
            }
        });
    }

    @Override
    public String getName() {
        return "routing/1.0";
    }
}
