package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class RRPTransformers {

    public static Request toRequest(TransportInboundMessage msg, Codecs codecs) {
        Request ret = new Request();
        ret.setId(msg.getId());
        return ret;
    }
    public static Response toResponse(TransportInboundMessage msg, Codecs codecs) {
        return new Response(msg.getId(), "hello");
    }
    public static TransportOutboundMessage toOutbound(Request request, Codecs codecs) {
        return new TransportOutboundMessage(request.getId(), null, "requestresponse");
    }
    public static TransportOutboundMessage toOutbound(Response response, Codecs codecs) {
        return new TransportOutboundMessage(response.getId(), null, "requestresponse");
    }
}
