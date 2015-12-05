package io.muoncore.protocol.introspection.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class IntrospectClientProtocol<X,R> {

    private Codecs codecs;

    public IntrospectClientProtocol(
            String serviceName,
            AutoConfiguration config,
            final ChannelConnection<ServiceExtendedDescriptor, String> leftChannelConnection,
            final ChannelConnection<TransportOutboundMessage, TransportInboundMessage> rightChannelConnection,
            final Codecs codecs) {

        rightChannelConnection.receive( message -> {
            if (message == null) {
                leftChannelConnection.shutdown();
                return;
            }
            ServiceExtendedDescriptor descript = codecs.decode(message.getPayload(), message.getContentType(), ServiceExtendedDescriptor.class);
            leftChannelConnection.send(descript);
        });

        leftChannelConnection.receive(request -> {
            if (request == null) {
                rightChannelConnection.shutdown();
                return;
            }
            TransportOutboundMessage msg = new TransportOutboundMessage(
                    "introspectionRequested",
                    UUID.randomUUID().toString(),
                    serviceName,
                    config.getServiceName(),
                    IntrospectionServerProtocolStack.PROTOCOL,
                    new HashMap<>(),
                    "text/plain",
                    new byte[0],
                    Arrays.asList(codecs.getAvailableCodecs()));

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
