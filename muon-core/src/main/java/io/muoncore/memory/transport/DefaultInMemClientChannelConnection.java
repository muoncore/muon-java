package io.muoncore.memory.transport;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.exception.MuonTransportFailureException;
import io.muoncore.memory.transport.bus.EventBus;
import io.muoncore.memory.transport.bus.Subscribe;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.transport.TransportEvents;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DefaultInMemClientChannelConnection implements InMemClientChannelConnection {

    private EventBus eventBus;
    private ChannelFunction<MuonInboundMessage> inboundFunction;

    private String targetService;
    private String protocol;

    private CountDownLatch handshakeControl = new CountDownLatch(1);
    private ChannelConnection<MuonInboundMessage, MuonOutboundMessage> serverChannel;

    public DefaultInMemClientChannelConnection(String targetService, String protocol, EventBus eventBus) {
        this.eventBus = eventBus;
        this.protocol = protocol;
        this.targetService = targetService;
        eventBus.register(this);
    }

    @Override
    public void receive(ChannelFunction<MuonInboundMessage> function) {
        this.inboundFunction = function;
        eventBus.post(new OpenChannelEvent(targetService, protocol, this));
    }

    @Subscribe public void on(InMemFailureEvent ev) {
      inboundFunction.apply(MuonMessageBuilder.fromService("local")
        .toService(targetService)
        .protocol(protocol)
        .step(TransportEvents.CONNECTION_FAILURE)
        .operation(MuonMessage.ChannelOperation.closed)
        .buildInbound());

      serverChannel.send(MuonMessageBuilder.fromService("local")
        .toService(targetService)
        .protocol(protocol)
        .step(TransportEvents.CONNECTION_FAILURE)
        .operation(MuonMessage.ChannelOperation.closed)
        .buildInbound());
    }

    @Override
    public void send(MuonOutboundMessage message) {
        try {
            handshakeControl.await(1, TimeUnit.SECONDS);
            if (serverChannel == null) {
                throw new MuonTransportFailureException("Server channel did not connect within the required timeout. This is a bug", new NullPointerException());
            }
            if (message == null) {
                serverChannel.shutdown();
            } else {
                serverChannel.send(message.toInbound());
            }
        } catch (InterruptedException e) {
            throw new MuonTransportFailureException("Unable to connect, no remote server attached to the channel within the timeout", e);
        }
    }

    @Override
    public void shutdown() {
      eventBus.unregister(this);
        if (serverChannel != null) {
            serverChannel.shutdown();
        }
    }

    @Override
    public void attachServerConnection(ChannelConnection<MuonInboundMessage, MuonOutboundMessage> serverChannel) {
        this.serverChannel = serverChannel;
        serverChannel.receive( msg -> {
            if (msg == null) {
                inboundFunction.apply(null);
                return;
            }
            inboundFunction.apply(msg.toInbound());
        });
        handshakeControl.countDown();
    }
}
