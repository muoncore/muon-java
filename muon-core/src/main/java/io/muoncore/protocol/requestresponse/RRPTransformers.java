package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.requestresponse.server.ServerRequest;
import io.muoncore.protocol.requestresponse.server.ServerResponse;

import java.util.HashMap;
import java.util.Map;

public class RRPTransformers {

    public final static String REQUEST_RESPONSE_PROTOCOL = "request";

    public static ServerRequest toRequest(MuonInboundMessage msg, Codecs codecs) {
        ServerRequest request = codecs.decode(msg.getPayload(), msg.getContentType(), ServerRequest.class);
        request.setCodecs(codecs);
        return request;
    }

    public static Response toResponse(MuonInboundMessage msg, Codecs codecs) {
        Response resp = codecs.decode(msg.getPayload(), msg.getContentType(), Response.class);
        resp.setCodecs(codecs);
        return resp;
    }

    @SuppressWarnings("unchecked")
    public static MuonOutboundMessage toOutbound(String thisService, Request request, Codecs codecs, String[] acceptEncodings) {

        Codecs.EncodingResult encodedPayload = codecs.encode(request.getPayload(), acceptEncodings);

        Map req = new HashMap<>();
        req.put("body", encodedPayload.getPayload());
        req.put("content_type", encodedPayload.getContentType());
        req.put("url", request.getUrl());

        Codecs.EncodingResult payload = codecs.encode(req, acceptEncodings);

        return MuonMessageBuilder
                .fromService(thisService)
                .step(RRPEvents.REQUEST)
                .protocol(REQUEST_RESPONSE_PROTOCOL)
                .toService(request.getUrl().getHost())
                .payload(payload.getPayload())
                .contentType(payload.getContentType())
                .status(MuonMessage.Status.success)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static MuonOutboundMessage toOutbound(String thisService, String targetService, ServerResponse response, Codecs codecs, String[] acceptEncodings) {

        Codecs.EncodingResult content = codecs.encode(response.getPayload(), acceptEncodings);

        Map outSchema = new HashMap<>();
        outSchema.put("status", response.getStatus());
        outSchema.put("body", content.getPayload());
        outSchema.put("content_type", content.getContentType());

        Codecs.EncodingResult payload = codecs.encode(outSchema, acceptEncodings);

        return MuonMessageBuilder
                .fromService(thisService)
                .toService(targetService)
                .protocol(REQUEST_RESPONSE_PROTOCOL)
                .step(RRPEvents.RESPONSE)
                .contentType(payload.getContentType())
                .payload(payload.getPayload())
                .status(MuonMessage.Status.success)
                .operation(MuonMessage.ChannelOperation.closed)
                .build();
    }
}
