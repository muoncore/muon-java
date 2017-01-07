package io.muoncore.transport.client;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.NoSuchServiceException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.sharedsocket.client.SharedSocketRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class MultiTransportClientChannelConnection implements ChannelConnection<MuonOutboundMessage, MuonInboundMessage> {

        private ChannelFunction<MuonInboundMessage> inbound;
    private Dispatcher dispatcher;

    private SharedSocketRouter router = null;
    private Discovery discovery;
    private TransportConnectionProvider transportConnectionProvider;

    private Map<String, ChannelConnection<MuonOutboundMessage, MuonInboundMessage>> channelConnectionMap = new HashMap<>();
    private Logger LOG = LoggerFactory.getLogger(MultiTransportClientChannelConnection.class.getCanonicalName());

    public MultiTransportClientChannelConnection(Dispatcher dispatcher, SharedSocketRouter router, Discovery discovery,
            TransportConnectionProvider transportConnectionProvider) {
        this.dispatcher = dispatcher;
        this.router = router;
        this.transportConnectionProvider = transportConnectionProvider;
        this.discovery = discovery;
    }

    @Override
    public void receive(ChannelFunction<MuonInboundMessage> function) {
        inbound = arg -> {
            dispatcher.tryDispatch(arg, function::apply, Throwable::printStackTrace);
        };
    }

    @Override
    public synchronized void send(MuonOutboundMessage message) {
        if (inbound == null) {
            throw new IllegalStateException("Transport connection is not in a complete state can cannot send data. The receive function has not been set");
        }
        if (message == null) {
            shutdown();
        } else {
            dispatcher.dispatch(message, msg -> {
                ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connection = channelConnectionMap.get(
                        key(message)
                );
                try {
                    if (connection == null) {
                        if (useSharedChannels(message)) {
                            connection = connectSharedChannel(message);
                        } else {
                            connection = transportConnectionProvider.connectChannel(
                                    message.getTargetServiceName(), message.getProtocol(), inbound);
                        }
                        if (connection == null) {
                            LOG.warn("Cannot open channel to service " + message.getTargetServiceName() + ", no transport accepted the message");
                            inbound.apply(MuonInboundMessage.serviceNotFound(msg));
                            return;
                        } else {
                            channelConnectionMap.put(key(message), connection);
                        }
                    }
                    connection.send(message);
                    if (message.getChannelOperation() == MuonMessage.ChannelOperation.closed) {
                        inbound = null;
                    }
                } catch (NoSuchServiceException ex) {
                    inbound.apply(MuonInboundMessage.serviceNotFound(msg));
                }
            }, Throwable::printStackTrace);
        }
    }

    private boolean useSharedChannels(MuonOutboundMessage message) {
        Optional<ServiceDescriptor> service = discovery.findService(
                serviceDescriptor -> serviceDescriptor.getIdentifier().equals(message.getTargetServiceName()));
//        if (service.isPresent()) {
//            return service.get().getCapabilities().contains(SharedSocketRouter.PROTOCOL);
//        }
        return true;
    }

    @Override
    public void shutdown() {
        dispatcher.dispatch(null, msg -> {
            channelConnectionMap.forEach((s, transportOutboundMessageTransportInboundMessageChannelConnection) -> {
                transportOutboundMessageTransportInboundMessageChannelConnection.shutdown();
            });
        }, Throwable::printStackTrace);
    }

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connectSharedChannel(MuonOutboundMessage message) {
        ChannelConnection<MuonOutboundMessage, MuonInboundMessage> connection = router.openClientChannel(message.getTargetServiceName());
        connection.receive(inbound);
        return connection;
    }

    private static String key(MuonOutboundMessage key) {
        return key.getTargetServiceName() + "_" + key.getProtocol();
    }
}
