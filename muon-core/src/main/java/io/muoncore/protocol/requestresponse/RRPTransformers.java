package io.muoncore.protocol.requestresponse;

import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

public class RRPTransformers {

    public static Request toRequest(TransportInboundMessage msg) {
        Request ret = new Request();
        ret.setId(msg.getId());
        return ret;
    }
    public static Response toResponse(TransportInboundMessage msg) {
        return new Response(msg.getId(), "hello");
    }
    public static TransportOutboundMessage toOutbound(Request request) {
        return new TransportOutboundMessage(request.getId(), null, "requestresponse");
    }
    public static TransportOutboundMessage toOutbound(Response response) {
        return new TransportOutboundMessage(response.getId(), null, "requestresponse");
    }
}
