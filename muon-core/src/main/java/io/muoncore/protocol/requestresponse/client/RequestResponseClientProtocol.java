package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.lang.reflect.Type;

/**
 * Request Response client middleware protocol.
 *
 * Add reliability, timeout etc.
 *
 */
public class RequestResponseClientProtocol<X,R> {

    private Codecs codecs;

    public RequestResponseClientProtocol(
            String serviceName,
            final ChannelConnection<Response<R>, Request<X>> leftChannelConnection,
            final ChannelConnection<TransportOutboundMessage, TransportInboundMessage> rightChannelConnection,
            final Type responseType,
            final Codecs codecs) {

        rightChannelConnection.receive( message -> {
            if (message == null) {
               leftChannelConnection.shutdown();
                return;
            }
            leftChannelConnection.send(
                    RRPTransformers.toResponse(message, codecs, responseType));
        });

        leftChannelConnection.receive(request -> {
            if (request == null) {
                rightChannelConnection.shutdown();
                return;
            }
            rightChannelConnection.send(RRPTransformers.toOutbound(
                    serviceName,
                    request, codecs, codecs.getAvailableCodecs()));
        });

        /**
         * handle 404.
         * handle local timeout.
         *
         *
         */
    }

}
