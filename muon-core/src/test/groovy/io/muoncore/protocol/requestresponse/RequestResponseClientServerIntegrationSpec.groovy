package io.muoncore.protocol.requestresponse

import io.muoncore.channel.Channels
import io.muoncore.channel.async.StandardAsyncChannel
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack
import io.muoncore.protocol.requestresponse.server.RequestResponseServerProtocolStack
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification

class RequestResponseClientServerIntegrationSpec extends Specification {

    def "client and server can communicate"() {
        given:

        def server = new RequestResponseServerProtocolStack()
        def channel = new StandardAsyncChannel()

        //does the conversion from outbound<=>inbound that the transport pair would ordinarily do.
        Channels.transform(
                server.createChannel(),
                channel.left(),
                {
                    new TransportInboundMessage(it.id, it.serviceName, it.protocol)
                },
                {
                    new TransportInboundMessage(it.id, it.serviceName, it.protocol)
                }
        )

        def transportClient = Mock(TransportClient) {
            openClientChannel() >> channel.right()
        }

        def client = new RequestResponseClientProtocolStack() {
            @Override
            TransportClient getTransportClient() {
                return transportClient
            }
        }

        when:
        Response response = client.request(new Request()).get()

        then:
        response.url == "/hello"
    }
}
