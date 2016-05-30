package io.muoncore.extension.amqp;

import io.muoncore.Discovery;
import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.Codecs;

public interface AmqpChannelFactory {
    void initialiseEnvironment(Codecs codecs, Discovery discovery, Scheduler scheduler);
    AmqpChannel createChannel();
}
