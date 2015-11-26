package io.muoncore.protocol.event.server;

import io.muoncore.Discovery;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.event.Event;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Collections;

/**
 * Server side of the event protocol
 */
public class EventServerProtocolStack implements
        ServerProtocolStack {

    private final ChannelConnection.ChannelFunction<Event >handler;
    private Codecs codecs;
    private Discovery discovery;

    public EventServerProtocolStack(ChannelConnection.ChannelFunction<Event> handler,
                                    Codecs codecs) {
        this.codecs = codecs;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChannelConnection<TransportInboundMessage, TransportOutboundMessage> createChannel() {

        Channel<TransportOutboundMessage, TransportInboundMessage> api2 = Channels.channel("eventserver", "transport");

        api2.left().receive( message -> {
//            RequestMetaData meta = RRPTransformers.toRequestMetaData(message);
//            RequestResponseServerHandler handler = handlers.findHandler(meta);
//
//            Request request = RRPTransformers.toRequest(message, codecs, handler.getRequestType());
//
//            handler.handle(new RequestWrapper() {
//                @Override
//                public Request getRequest() {
//                    return request;
//                }
//
//                @Override
//                public void answer(Response response) {
//                    ServiceDescriptor target = discovery.findService(svc ->
//                            svc.getIdentifier().equals(
//                                    request.getMetaData().getSourceService())).get();
//
//                    TransportOutboundMessage msg = RRPTransformers.toOutbound(request.getMetaData().getTargetService(), request.getMetaData().getSourceService(), response, codecs,
//                            target.getCodecs());
//
//                    api2.left().send(msg);
//                }
//            });
        });

        return api2.right();
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {

        return new ProtocolDescriptor(
                "event",
                "Event Sink Protocol",
                "Provides a discoverable sink for events to flow into without needing  explicit service endpoints",
                Collections.emptyList());
    }
}
