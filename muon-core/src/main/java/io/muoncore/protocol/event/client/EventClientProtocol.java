package io.muoncore.protocol.event.client;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.protocol.event.Event;
import io.muoncore.protocol.requestresponse.Request;
import io.muoncore.protocol.requestresponse.RequestMetaData;
import io.muoncore.protocol.requestresponse.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This middleware will accept an Event. It will then attempt to locate an event store to send a persistence Request to
 */
public class EventClientProtocol<X> {

    public EventClientProtocol(
            AutoConfiguration configuration,
            Discovery discovery,
            ChannelConnection<Response<Map>, Event<X>> leftChannelConnection,
            ChannelConnection<Request<Event<X>>, Response<Map>> rightChannelConnection) {

        rightChannelConnection.receive( message -> {
            leftChannelConnection.send(message);
        });

        leftChannelConnection.receive(event -> {
            Optional<ServiceDescriptor> eventService = discovery.findService( service -> service.getTags().contains("eventstore"));

            if (!eventService.isPresent()) {
                leftChannelConnection.send(new Response<>(404, new HashMap<>()));
            } else {
                Request<Event<X>> msg = new Request<>(
                        new RequestMetaData(
                                "/",
                                configuration.getServiceName(),
                                eventService.get().getIdentifier()), event);
                msg.setId(event.getId());
                rightChannelConnection.send(msg);
            }
        });
    }
}
