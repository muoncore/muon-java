package io.muoncore.protocol.event.server;

import io.muoncore.Discovery;
import io.muoncore.channel.Channel;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.channel.Channels;
import io.muoncore.codec.Codecs;
import io.muoncore.descriptors.ProtocolDescriptor;
import io.muoncore.protocol.ServerProtocolStack;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.EventCodec;
import io.muoncore.protocol.event.EventProtocolMessages;
import io.muoncore.protocol.event.client.EventResult;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Server side of the event protocol
 */
public class EventServerProtocolStack implements
        ServerProtocolStack {

    private final ChannelConnection.ChannelFunction<EventWrapper>handler;
    private Codecs codecs;
    private Discovery discovery;

    public EventServerProtocolStack(ChannelConnection.ChannelFunction<EventWrapper> handler,
                                    Codecs codecs) {
        this.codecs = codecs;
        this.handler = handler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChannelConnection<MuonInboundMessage, MuonOutboundMessage> createChannel() {

        Channel<MuonOutboundMessage, MuonInboundMessage> api2 = Channels.channel("eventserver", "transport");

        api2.left().receive( message -> {
            if (message == null) {
                return;
            }

            Map data = codecs.decode(message.getPayload(), message.getContentType(), Map.class);
            Event ev = EventCodec.getEventFromMap(data);

            Channel<EventResult, EventWrapper> evserver = Channels.channel("eventserverapp", "wrapper");
            EventWrapper wrapper = new EventWrapper(ev, evserver.left());

            evserver.right().receive( eventResult -> {

                Codecs.EncodingResult result = codecs.encode(eventResult, message.getSourceAvailableContentTypes().toArray(
                        new String[message.getSourceAvailableContentTypes().size()]));

                api2.left().send(new MuonOutboundMessage(
                        EventProtocolMessages.PERSISTED,
                        message.getId(),
                        message.getSourceServiceName(),
                        message.getTargetServiceName(),
                        EventProtocolMessages.PROTOCOL,
                        Collections.EMPTY_MAP,
                        result.getContentType(),
                        result.getPayload(),
                        Arrays.asList(codecs.getAvailableCodecs())));
            });

            handler.apply(wrapper);
        });

        return api2.right();
    }

    @Override
    public ProtocolDescriptor getProtocolDescriptor() {

        return new ProtocolDescriptor(
                EventProtocolMessages.PROTOCOL,
                "Event Sink Protocol",
                "Provides a discoverable sink for events to flow into without needing explicit service endpoints",
                Collections.emptyList());
    }
}
