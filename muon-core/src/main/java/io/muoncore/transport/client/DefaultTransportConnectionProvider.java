package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.MuonTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DefaultTransportConnectionProvider implements TransportConnectionProvider {

    private Logger logger = LoggerFactory.getLogger(DefaultTransportConnectionProvider.class);
    private List<MuonTransport> transports;

    public DefaultTransportConnectionProvider(List<MuonTransport> transports) {
        this.transports = transports;
    }

    public ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connectChannel(
            String service,
            String protocol, ChannelConnection.ChannelFunction<MuonInboundMessage> inbound) {

        Optional<MuonTransport> transport = transports.stream().filter(tr -> tr.canConnectToService(service)).findFirst();

        if (transport.isPresent()) {
            ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connection = transport.get().openClientChannel(service, protocol);
            connection.receive(inbound);
            return connection;
        } else {
            logger.warn("Can't find transport that can reach service " + service);
            return null;
        }
    }
}
