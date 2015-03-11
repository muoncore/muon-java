package org.muoncore.extension.streamcontrol;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;
import org.muoncore.MuonService;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.transports.MuonStreamRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow inspection and some runtime control of the the reactive streaming
 */
public class StreamControlExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {
        muonApi.getMuon().onGet("/reactive-streams",
                "The various streams available",
                new MuonService.MuonGet() {
                    @Override
                    public Object onQuery(MuonResourceEvent queryEvent) {
                        List streams = new ArrayList();

                        for (MuonStreamRegister stream: muonApi.getStreams()) {
                            streams.add(stream.getName());
                        }

                        return JSON.toString(streams);
                    }
                });
    }

    @Override
    public String getName() {
        return "streamcontrol";
    }
}
