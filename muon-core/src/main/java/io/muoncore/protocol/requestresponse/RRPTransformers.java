package io.muoncore.protocol.requestresponse;

import io.muoncore.codec.Codecs;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;

import java.lang.reflect.Type;

public class RRPTransformers {

    public final static String REQUEST_RESPONSE_PROTOCOL = "request";

    public static Headers toRequestMetaData(MuonInboundMessage msg, Codecs codecs) {
        HeaderWrapper wrapper = codecs.decode(msg.getPayload(), msg.getContentType(), HeaderWrapper.class);
        return wrapper.getHeaders();
    }

    public static Request toRequest(MuonInboundMessage msg, Codecs codecs, Type type) {
        return codecs.decode(msg.getPayload(), msg.getContentType(), new RequestParameterizedType(type));
    }

    public static <T> Response<T> toResponse(MuonInboundMessage msg, Codecs codecs, Type type) {
        return codecs.decode(msg.getPayload(), msg.getContentType(), new ResponseParameterizedType(type));
    }

    public static MuonOutboundMessage toOutbound(String thisService, Request request, Codecs codecs, String[] acceptEncodings) {

        Codecs.EncodingResult payload = codecs.encode(request, acceptEncodings);

        return MuonMessageBuilder
                .fromService(thisService)
                .step("RequestMade")
                .protocol(REQUEST_RESPONSE_PROTOCOL)
                .toService(request.getHeaders().getTargetService())
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
                .operation(MuonMessage.ChannelOperation.closed)
                .build();
    }

    static class HeaderWrapper {
        private Headers headers;

        public HeaderWrapper(Headers headers) {
            this.headers = headers;
        }

        public Headers getHeaders() {
            return headers;
        }
    }
}
