package io.muoncore.transport.client

import io.muoncore.transport.MuonTransport
import reactor.Environment
import spock.lang.Specification

class SingleTransportClientSpec extends Specification {

    def "client creates a SingleTransportChannelConnection for every request"() {
        Environment.initializeIfEmpty()

        def transport = Mock(MuonTransport)
        def dispatcher = Mock(TransportMessageDispatcher)

        def cl = new SingleTransportClient(transport, dispatcher)

        expect:
        cl.openClientChannel() instanceof SingleTransportClientChannelConnection
    }
}
