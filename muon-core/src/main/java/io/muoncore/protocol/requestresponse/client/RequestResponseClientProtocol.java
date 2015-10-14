package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Request Response client middleware protocol.
 *
 * Add reliability, timeout etc.
 *
 */
public class RequestResponseClientProtocol<X> {

    private Codecs codecs;

    public RequestResponseClientProtocol(
            final ChannelConnection<Response, Request<X>> leftChannelConnection,
            final ChannelConnection<TransportOutboundMessage, TransportInboundMessage> rightChannelConnection,
            final Codecs codecs) {

        rightChannelConnection.receive( message -> {
            leftChannelConnection.send(
                RRPTransformers.toResponse(message, codecs));
        });

        leftChannelConnection.receive(request -> {
            rightChannelConnection.send(RRPTransformers.toOutbound(request, codecs));
        });

        /**
         * handle 404.
         * handle local timeout.
         *
         *
         */
    }

}
