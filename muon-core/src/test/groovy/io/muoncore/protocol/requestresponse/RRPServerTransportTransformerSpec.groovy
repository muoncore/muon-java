package io.muoncore.protocol.requestresponse

import io.muoncore.channel.async.StandardAsyncChannel
import spock.lang.Specification

class RRPServerTransportTransformerSpec extends Specification {

    def "transformer converts TransportInboundMessages into Requests"() {

        def rightConnection = new StandardAsyncChannel();
        def leftConnection = new StandardAsyncChannel();

        def transformer = new RRPServerTransportTransformerSpec()

    }

}
