package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RRPTransformers {

    public final static String REQUEST_RESPONSE_PROTOCOL = "request";

    public static RequestMetaData toRequestMetaData(TransportInboundMessage msg) {
        return new RequestMetaData(
                msg.getMetadata().get(Request.URL),
                msg.getSourceServiceName(),
                msg.getTargetServiceName());
    }

    public static Request toRequest(TransportInboundMessage msg, Codecs codecs, Type type) {
        Request ret = new Request<>(
                new RequestMetaData(
                        msg.getMetadata().get(Request.URL),
                        msg.getSourceServiceName(),
                        msg.getTargetServiceName()),
                codecs.decode(msg.getPayload(), msg.getContentType(), type)
        );
        ret.setId(msg.getId());
        return ret;
    }

    public static <T> Response<T> toResponse(TransportInboundMessage msg, Codecs codecs, Type type) {
        return new Response<>(
                Integer.parseInt(msg.getMetadata().get(Response.STATUS)),
                codecs.decode(msg.getPayload(),
                        msg.getContentType(), type));
    }

    public static TransportOutboundMessage toOutbound(String thisService, Request request, Codecs codecs, String[] acceptEncodings) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put(Request.URL, request.getMetaData().getUrl());

        Codecs.EncodingResult payload = codecs.encode(request.getPayload(), acceptEncodings);

        return new TransportOutboundMessage(
                "requestMade",
                request.getId(),
                request.getMetaData().getTargetService(),
                thisService,
                REQUEST_RESPONSE_PROTOCOL,
                metadata,
                payload.getContentType(),
                payload.getPayload(), Arrays.asList(codecs.getAvailableCodecs()));
    }

    public static TransportOutboundMessage toOutbound(String thisService, String targetService, Response response, Codecs codecs, String[] acceptEncodings) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put(Response.STATUS, String.valueOf(response.getStatus()));

        Codecs.EncodingResult payload = codecs.encode(response.getPayload(), acceptEncodings);

        return new TransportOutboundMessage(
                "responseSent",
                response.getId(),
                targetService,
                thisService,
                REQUEST_RESPONSE_PROTOCOL,
                metadata,
                payload.getContentType(),
                payload.getPayload(), Arrays.asList(codecs.getAvailableCodecs()));
    }
}
