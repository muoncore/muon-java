package io.muoncore.future

import spock.lang.Specification

class ChannelConnectionBackedFutureSpec extends Specification {

    def "channelbacked future not tested"() {
        //use the channbel backed future all over the place.
        expect:
        throw new IllegalStateException("Not tested!")
    }
}
