package io.muoncore.extension.streamcontrol;

import io.muoncore.MuonExtension;
import io.muoncore.future.MuonFuture;
import io.muoncore.future.MuonFutures;
import io.muoncore.MuonService;
import io.muoncore.transport.resource.MuonResourceEvent;
import io.muoncore.transport.stream.MuonStreamRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Allow inspection and some runtime control of the the reactive streaming
 */
public class StreamControlExtension implements MuonExtension {

    @Override
    public void extend(final MuonService muonApi) {
        muonApi.onGet("/reactive-streams",
                Map.class,
                new MuonService.MuonGet<Map>() {
                    @Override
                    public MuonFuture onQuery(MuonResourceEvent<Map> queryEvent) {
                        List<String> streams = new ArrayList<String>();

                        for (MuonStreamRegister stream : muonApi.getStreams()) {
                            streams.add(stream.getName());
                        }

                        return MuonFutures.immediately(streams);
                    }
                });
    }
}
