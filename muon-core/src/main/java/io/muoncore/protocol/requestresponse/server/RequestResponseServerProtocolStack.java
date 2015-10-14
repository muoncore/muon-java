package io.muoncore.protocol.requestresponse.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.async.StandardAsyncChannel;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.requestresponse.RRPTransformers;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

/**
 * Server side of the Requestr Response protocol.
 *
 * Transports open channels on this protocol when a remote request response client opens a channel through them
 * and sends a first message.
 */
public class RequestResponseServerProtocolStack implements
        ServerProtocolStack {

    private final RequestResponseHandlers handlers;

    public RequestResponseServerProtocolStack(RequestResponseHandlers handlers) {
        this.handlers = handlers;
    }

    @Override
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {

        StandardAsyncChannel<TransportOutboundMessage, TransportInboundMessage> api2 = new StandardAsyncChannel<>();

        api2.left().receive( message -> {
            Request request = RRPTransformers.toRequest(message);
            RequestResponseServerHandler handler = handlers.findHandler(request);
            handler.handle(new RequestWrapper() {
                @Override
                public Request getRequest() {
                    return request;
                }

                @Override
                public void answer(Response response) {
                    TransportOutboundMessage msg = RRPTransformers.toOutbound(response);
                    api2.left().send(msg);
                }
            });
        });

        return api2.right();
    }
}
