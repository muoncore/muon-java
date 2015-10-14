package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
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

    public RequestResponseClientProtocol(final ChannelConnection<Response, Request<X>> leftChannelConnection,
              final ChannelConnection<TransportOutboundMessage, TransportInboundMessage> rightChannelConnection) {

        rightChannelConnection.receive( message -> {
            leftChannelConnection.send(
                RRPTransformers.toResponse(message));
        });

        leftChannelConnection.receive(request -> {
            rightChannelConnection.send(RRPTransformers.toOutbound(request));
        });

        /**
         * handle 404.
         * handle local timeout.
         *
         *
         */
    }

}
