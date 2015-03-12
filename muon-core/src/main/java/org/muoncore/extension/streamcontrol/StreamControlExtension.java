package org.muoncore.extension.streamcontrol;

import org.muoncore.MuonExtension;
import org.muoncore.MuonExtensionApi;
import org.muoncore.MuonService;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.transports.MuonStreamRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allow inspection and some runtime control of the the reactive streaming
 */
public class StreamControlExtension implements MuonExtension {

    @Override
    public void init(final MuonExtensionApi muonApi) {
        muonApi.getMuon().onGet("/reactive-streams",
                Map.class,
                new MuonService.MuonGet<Map>() {
                    @Override
                    public Object onQuery(MuonResourceEvent<Map> queryEvent) {
                        List<String> streams = new ArrayList<String>();

                        for (MuonStreamRegister stream: muonApi.getStreams()) {
                            streams.add(stream.getName());
                        }

                        return streams;
                    }
                });
    }

    @Override
    public String getName() {
        return "streamcontrol";
    }
}
