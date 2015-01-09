package org.muoncore.extension.eventlogger;

import org.muoncore.*;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonResourceEvent;

import java.util.ArrayList;
import java.util.List;

public class EventLoggerExtension implements MuonExtension {

    List<String> resources = new ArrayList<String>();

    @Override
    public void init(MuonExtensionApi muonApi) {
        muonApi.getDispatcher().addListener(new Dispatcher.Listener() {
            @Override
            public void presend(MuonMessageEvent event) {
                resources.add(event.getEventName());
            }
        });

        muonApi.getMuon().onGet("/muon/logger/history", "Get the history of logs", new MuonService.MuonGet() {
            @Override
            public Object onQuery(MuonResourceEvent queryEvent) {
                String ret = "<html><body>Yo<ul>";
                for (String res : resources) {
                    ret += "<li>" + res + "</li>\n";
                }
                ret += "</ul></body></html>";
                return ret;
            }
        });
    }

    @Override
    public String getName() {
        return "logger/1.0";
    }
}
