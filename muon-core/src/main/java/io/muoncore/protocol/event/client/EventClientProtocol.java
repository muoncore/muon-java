package io.muoncore.protocol.event.client;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.event.EventProtocolMessages;
import io.muoncore.transport.TransportEvents;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

/**
 * This middleware will accept an Event. It will then attempt to locate an event store to send a persistence Request to
 */
public class EventClientProtocol<X> {

    public EventClientProtocol(
            AutoConfiguration configuration,
            Discovery discovery,
            Codecs codecs,
            ChannelConnection<EventResult, Event<X>> leftChannelConnection,
            ChannelConnection<TransportOutboundMessage, TransportInboundMessage> rightChannelConnection) {

        rightChannelConnection.receive( message -> {
            if (message == null) {
                leftChannelConnection.shutdown();
                return;
            }

            EventResult result;

            switch(message.getType()) {
                case TransportEvents.SERVICE_NOT_FOUND:
                    result = new EventResult(EventResult.EventResultStatus.FAILED);
                    break;

                case TransportEvents.PROTOCOL_NOT_FOUND:
                    result = new EventResult(EventResult.EventResultStatus.FAILED);
                    break;

                default:
                    result = new EventResult(EventResult.EventResultStatus.PERSISTED);
            }

            //TODO, error handling
            /*
              timeout?
             */

            leftChannelConnection.send(result);
            leftChannelConnection.shutdown();
        });

        leftChannelConnection.receive(event -> {
            if (event == null) {
                rightChannelConnection.shutdown();
                return;
            }
            Optional<ServiceDescriptor> eventService = discovery.findService( service -> service.getTags().contains("eventstore"));

            if (!eventService.isPresent()) {
                //TODO, a failure, no event store available.
                leftChannelConnection.send(new EventResult(EventResult.EventResultStatus.FAILED));
            } else {

                Codecs.EncodingResult result = codecs.encode(event, eventService.get().getCodecs());

                TransportOutboundMessage msg = new TransportOutboundMessage(
                        event.getEventType(),
                        event.getId(),
                        eventService.get().getIdentifier(),
                        configuration.getServiceName(),
                        EventProtocolMessages.PROTOCOL,
                        new HashMap<>(),
                        result.getContentType(),
                        result.getPayload(),
                        Arrays.asList(codecs.getAvailableCodecs()));

                rightChannelConnection.send(msg);
            }
        });
    }
}
