package io.muoncore.transport.client

import io.muoncore.Discovery
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.transport.MuonTransport
import reactor.Environment
import spock.lang.Specification

class SingleTransportClientSpec extends Specification {

    def "client creates a SingleTransportChannelConnection for every request"() {
        Environment.initializeIfEmpty()

        def transport = Mock(MuonTransport)
        def dispatcher = Mock(TransportMessageDispatcher)
        def config = new AutoConfiguration()

        def cl = new MultiTransportClient([transport], dispatcher, config, Mock(Discovery), Mock(Codecs))

        expect:
        cl.openClientChannel() != null
    }
}
