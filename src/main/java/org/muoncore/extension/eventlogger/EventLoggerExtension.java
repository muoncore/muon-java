package org.muoncore.extension.eventlogger;

import org.muoncore.*;

import java.util.ArrayList;
import java.util.List;

public class EventLoggerExtension implements MuonExtension {

    List<String> resources = new ArrayList<String>();

    @Override
    public void init(MuonExtensionApi muonApi) {
        muonApi.getDispatcher().addListener(new Dispatcher.Listener() {
            @Override
            public void presend(MuonEvent event) {
                resources.add(event.getResource());
            }
        });

        muonApi.getMuon().resource("/muon/logger/history", "Get the history of logs", new Muon.MuonGet() {
            @Override
            public Object onQuery(Object queryEvent) {
                String ret = "<ul>";
                for(String res: resources) {
                    ret += "<li>" + res + "</li>\n" ;
                }
                ret += "</ul>";
                return ret;
            }
        });
    }
}
