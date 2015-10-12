package io.muoncore.protocol.requestresponse.client;

import io.muoncore.channel.ChannelConnection;
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

        rightChannelConnection.receive( message -> {leftChannelConnection.send(
                new Response("hello"));
        });

        leftChannelConnection.receive(request -> {
            TransportOutboundMessage msg = new TransportOutboundMessage(
                    request.getId(),
                    "serviceName",
                    "protocol"
            );
//            msg.setId(request.getId());
            rightChannelConnection.send(msg);
        });

        /**
         * handle 404.
         * handle local timeout.
         *
         *
         */
    }

}
