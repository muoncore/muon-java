package io.muoncore.transport.client;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportMessage;
import io.muoncore.transport.TransportOutboundMessage;
import reactor.core.Dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

class MultiTransportClientChannelConnection implements ChannelConnection<TransportOutboundMessage, TransportInboundMessage> {

    private List<MuonTransport> transports;
    private ChannelFunction<TransportInboundMessage> inbound;
    private Dispatcher dispatcher;

    private Map<String, ChannelConnection<TransportOutboundMessage, TransportInboundMessage>> channelConnectionMap = new HashMap<>();
    private Logger LOG = Logger.getLogger(MultiTransportClientChannelConnection.class.getCanonicalName());

    public MultiTransportClientChannelConnection(
            List<MuonTransport> transports, Dispatcher dispatcher) {
        this.transports = transports;
        this.dispatcher = dispatcher;
    }

    @Override
    public void receive(ChannelFunction<TransportInboundMessage> function) {
        inbound = arg -> {
            dispatcher.tryDispatch(arg, function::apply, Throwable::printStackTrace);
        };
    }

    @Override
    public synchronized void send(TransportOutboundMessage message) {
        if (inbound == null) {
            throw new IllegalStateException("Transport connection is not in a complete state can cannot send data. The receive function has not been set");
        }
        if (message == null) {
            shutdown();
        } else {
            dispatcher.dispatch(message, msg -> {
                ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connection = channelConnectionMap.get(
                        key(message)
                );
                try {
                    if (connection == null) {
                        connection = connectChannel(message);
                        if (connection == null) {
                            LOG.warning("Cannot open channel to service " + message.getTargetServiceName() + ", no transport accepted the message");
                            inbound.apply(TransportInboundMessage.serviceNotFound(msg));
                            return;
                        } else {
                            channelConnectionMap.put(key(message), connection);
                        }
                    }
                    connection.send(message);
                    if (message.getChannelOperation() == TransportMessage.ChannelOperation.CLOSE_CHANNEL) {
                        inbound = null;
                        this.transports = null;
                    }
                } catch (NoSuchServiceException ex) {
                    inbound.apply(TransportInboundMessage.serviceNotFound(msg));
                }
            }, Throwable::printStackTrace);
        }
    }

    @Override
    public void shutdown() {
        dispatcher.dispatch(null, msg -> {
            channelConnectionMap.forEach((s, transportOutboundMessageTransportInboundMessageChannelConnection) -> {
                transportOutboundMessageTransportInboundMessageChannelConnection.shutdown();
            });
        }, Throwable::printStackTrace);
    }

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connectChannel(TransportOutboundMessage message) {

        Optional<MuonTransport> transport = transports.stream().filter( tr -> tr.canConnectToService(message.getTargetServiceName())).findFirst();

        if (transport.isPresent()) {
            ChannelConnection<TransportOutboundMessage, TransportInboundMessage> connection = transport.get().openClientChannel(message.getTargetServiceName(), message.getProtocol());
            connection.receive(inbound);
            return connection;
        } else {
            return null;
        }
    }

    private static String key(TransportOutboundMessage key) {
        return key.getTargetServiceName() + "_" + key.getProtocol();
    }
}
