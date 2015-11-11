package io.muoncore.protocol.reactivestream.server

import spock.lang.Specification

class ReactiveStreamServerStackSpec extends Specification {

    def "not tested"() {
        expect:
        1 ==2
    }

    //if publisher exists, send ACK and connect the channel to it.

    //if publisher doesn't exist, send NACK down the channel and close it.

}
