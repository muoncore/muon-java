package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RRPTransformers {

    public final static String REQUEST_RESPONSE_PROTOCOL = "requestresponse";

    public static <T> RequestMetaData toRequestMetaData(TransportInboundMessage msg) {
        return new RequestMetaData(msg.getMetadata().get(Request.URL), msg.getSourceServiceName());
    }

    public static <T> Request toRequest(TransportInboundMessage msg, Codecs codecs, Class<T> type) {
        Request ret = new Request<>(
                new RequestMetaData(msg.getMetadata().get(Request.URL), msg.getSourceServiceName()),
                codecs.decode(msg.getPayload(), msg.getContentType(), type)
        );
        ret.setId(msg.getId());
        return ret;
    }

    public static <T> Response<T> toResponse(TransportInboundMessage msg, Codecs codecs, Class<T> type) {
        return new Response<>(
                Integer.parseInt(msg.getMetadata().get(Response.STATUS)),
                codecs.decode(msg.getPayload(),
                        msg.getContentType(), type));
    }

    public static TransportOutboundMessage toOutbound(String thisService, Request request, Codecs codecs, String[] acceptEncodings) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put(Request.URL, request.getMetaData().getUrl());

        Codecs.EncodingResult payload = null;
        try {
            payload = codecs.encode(request.getPayload(), acceptEncodings);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new TransportOutboundMessage(
                "requestMade",
                request.getId(),
                thisService,
                REQUEST_RESPONSE_PROTOCOL,
                metadata,
                payload.getContentType(),
                payload.getPayload());
    }

    public static TransportOutboundMessage toOutbound(String thisService, Response response, Codecs codecs, String[] acceptEncodings) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put(Response.STATUS, String.valueOf(response.getStatus()));

        Codecs.EncodingResult payload = null;
        try {
            payload = codecs.encode(response.getPayload(), acceptEncodings);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new TransportOutboundMessage(
                "responseSent",
                response.getId(),
                thisService,
                REQUEST_RESPONSE_PROTOCOL,
                metadata,
                payload.getContentType(),
                payload.getPayload());
    }
}
