package io.muoncore.protocol.introspection.client

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.lang.Timeout

@Timeout(1)
class IntrospectionClientProtocolStackSpec extends Specification {

    def "introspection client"() {

        def func
        def codecs = new JsonOnlyCodecs()
        def discovery = Mock(Discovery) {
            getCodecsForService(_) >> ["application/json"] as String[]
        }
        def client = Mock(ChannelConnection) {
            receive(_) >> { args ->
                func = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def config = new AutoConfiguration(serviceName: "tombola")
        def transClient = Mock(TransportClient) {
            openClientChannel() >> client
        }

        def stack = new IntrospectionClientProtocolStack() {
            @Override
            Discovery getDiscovery() {
                return discovery
            }

            @Override
            Codecs getCodecs() {
                return codecs
            }

            @Override
            AutoConfiguration getConfiguration() {
                return config
            }

            @Override
            TransportClient getTransportClient() {
                return transClient
            }
        }

        when:
        Thread.start {
            sleep 100
            func(new MuonInboundMessage(
                    "introspectionReport",
                    UUID.randomUUID().toString(),
                    "simples",
                    "tombola",
                    IntrospectionServerProtocolStack.PROTOCOL,
                    new HashMap<>(),
                    "application/json",
                    codecs.encode(new ServiceExtendedDescriptor("tombola", [new ProtocolDescriptor("rrp", "Crazy Proto", "B happy", [])]), ["application/json"] as String[]).payload,
                    ["application/json"], MuonMessage.ChannelOperation.NORMAL
            ))
        }
        ServiceExtendedDescriptor descriptor = stack.introspect("simples").get()

        then:
        descriptor
        descriptor.serviceName == "tombola"
        descriptor.protocols.size() == 1
    }
}
