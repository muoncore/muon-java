package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;
import io.muoncore.transport.client.RingBufferLocalDispatcher;
import reactor.core.Dispatcher;

public class DefaultAmqpChannelFactory implements AmqpChannelFactory {

    private String localServiceName;
    private QueueListenerFactory listenerFactory;
    private AmqpConnection connection;
    private Dispatcher dispatcher = new RingBufferLocalDispatcher("amqp-channel", 32768);
    private Codecs codecs;
    private Discovery discovery;
    private Scheduler scheduler;

    public DefaultAmqpChannelFactory(String localServiceName, QueueListenerFactory listenerFactory, AmqpConnection connection) {
        this.localServiceName = localServiceName;
        this.listenerFactory = listenerFactory;
        this.connection = connection;
    }

    @Override
    public AmqpChannel createChannel() {
        return new DefaultAmqpChannel(connection, listenerFactory, localServiceName, dispatcher, codecs, discovery, scheduler);
    }

    @Override
    public void initialiseEnvironment(Codecs codecs, Discovery discovery, Scheduler scheduler) {
        this.codecs = codecs;
        this.discovery = discovery;
        this.scheduler = scheduler;
    }
}
