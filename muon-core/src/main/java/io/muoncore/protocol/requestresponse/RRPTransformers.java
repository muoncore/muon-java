package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;

import java.lang.reflect.Type;

public class RRPTransformers {

    public final static String REQUEST_RESPONSE_PROTOCOL = "request";

    public static RequestMetaData toRequestMetaData(MuonInboundMessage msg, Codecs codecs) {
        return codecs.decode(msg.getPayload(), msg.getContentType(), RequestMetaData.class);
    }

    public static Request toRequest(MuonInboundMessage msg, Codecs codecs, Type type) {
        return codecs.decode(msg.getPayload(), msg.getContentType(), new RequestParameterizedType(type));
    }

    public static <T> Response<T> toResponse(MuonInboundMessage msg, Codecs codecs, Type type) {
        return codecs.decode(msg.getPayload(), msg.getContentType(), new RequestParameterizedType(type));
    }

    public static MuonOutboundMessage toOutbound(String thisService, Request request, Codecs codecs, String[] acceptEncodings) {

        Codecs.EncodingResult payload = codecs.encode(request.getPayload(), acceptEncodings);

        return MuonMessageBuilder
                .fromService(thisService)
                .step("introspectionRequested")
                .protocol(REQUEST_RESPONSE_PROTOCOL)
                .toService(request.getMetaData().getTargetService())
                .payload(payload.getPayload())
                .contentType(payload.getContentType())
                .status(MuonMessage.Status.success)
                .build();
    }

    public static MuonOutboundMessage toOutbound(String thisService, String targetService, Response response, Codecs codecs, String[] acceptEncodings) {

        Codecs.EncodingResult payload = codecs.encode(response, acceptEncodings);

        return MuonMessageBuilder
                .fromService(thisService)
                .toService(targetService)
                .protocol(REQUEST_RESPONSE_PROTOCOL)
                .step(RRPEvents.RESPONSE)
                .contentType(payload.getContentType())
                .payload(payload.getPayload())
                .status(MuonMessage.Status.success)
                .operation(MuonMessage.ChannelOperation.CLOSE_CHANNEL)
                .build();
    }
}
