package io.muoncore.transport.client

import spock.lang.Specification

class SingleTransportClientSpec extends Specification {

    def "Single transport is not tested"() {
        expect:
        throw new IllegalStateException("Broken")
    }

}
