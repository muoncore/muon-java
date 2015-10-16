package io.muoncore.extension.amqp

import spock.lang.Specification

class DefaultAmqpChannelFactorySpec extends Specification {

    def "broken"() {
        expect:
        throw new IllegalStateException("Not tested!")
    }

}
