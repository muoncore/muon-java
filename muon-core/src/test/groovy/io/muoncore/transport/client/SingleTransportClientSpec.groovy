package io.muoncore.transport.client

import io.muoncore.transport.MuonTransport
import spock.lang.Specification

class SingleTransportClientSpec extends Specification {

    def "client creates a SingleTransportChannelConnection for every request"() {
        def transport = Mock(MuonTransport)
        def cl = new SingleTransportClient(transport)

        expect:
        cl.openClientChannel() instanceof SingleTransportChannelConnection
    }

}
