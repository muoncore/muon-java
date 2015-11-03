package io.muoncore.extension.amqp

import spock.lang.Specification

class DefaultAmqpChannelFactorySpec extends Specification {

    def "factory makes channels"() {
        def factory = new DefaultAmqpChannelFactory("simpleservice", Mock(QueueListenerFactory), Mock(AmqpConnection))

        when:
        def channel = factory.createChannel()

        then:
        channel != null
        channel instanceof DefaultAmqpChannel
    }

}
