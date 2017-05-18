package io.muoncore.protocol.introspection.client;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.descriptors.ServiceExtendedDescriptor;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;

import static io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack.PROTOCOL;

public class IntrospectClientProtocol<X,R> {

  public static final String INTROSPECTION_REQUESTED = "introspectionRequested";
  private Codecs codecs;

    public IntrospectClientProtocol(
            String serviceName,
            AutoConfiguration config,
            final ChannelConnection<ServiceExtendedDescriptor, String> leftChannelConnection,
            final ChannelConnection<MuonOutboundMessage, MuonInboundMessage> rightChannelConnection,
            final Codecs codecs,
            final Discovery discovery) {

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

            Codecs.EncodingResult result = codecs.encode(new Object(),
                    discovery.getCodecsForService(serviceName));

            MuonOutboundMessage msg = MuonMessageBuilder
                    .fromService(config.getServiceName())
                    .step(INTROSPECTION_REQUESTED)
                    .protocol(PROTOCOL)
                    .toService(serviceName)
                    .payload(result.getPayload())
                    .contentType(result.getContentType())
                    .status(MuonMessage.Status.success)
                    .build();

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
