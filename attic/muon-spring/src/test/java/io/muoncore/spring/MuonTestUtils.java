package io.muoncore.spring;

import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerRequest;
import io.muoncore.spring.model.MuonTestRequestWrapper;

import java.net.URI;
import java.net.URISyntaxException;

public class MuonTestUtils {
    public static <T> RequestWrapper getSampleMuonRequestWrapper(T payload) throws URISyntaxException {

        Codecs codecs = new JsonOnlyCodecs();
        Codecs.EncodingResult result = codecs.encode(payload, codecs.getAvailableCodecs());

        final ServerRequest request = new ServerRequest(
                    new URI("muon://service/resource"), result.getPayload(), result.getContentType(), codecs);
        return new MuonTestRequestWrapper(request);
    }
}
