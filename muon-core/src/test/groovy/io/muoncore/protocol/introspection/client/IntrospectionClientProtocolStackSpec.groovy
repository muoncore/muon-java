package io.muoncore.protocol.introspection.client

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.descriptors.ProtocolDescriptor
import io.muoncore.descriptors.SchemaDescriptor
import io.muoncore.descriptors.SchemasDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.introspection.server.IntrospectionServerProtocolStack
import io.muoncore.transport.client.TransportClient
import spock.lang.Specification
import spock.lang.Timeout

@Timeout(1)
class IntrospectionClientProtocolStackSpec extends Specification {

    def "introspection client"() {

        def func
        def codecs = new JsonOnlyCodecs()
        def discovery = Mock(Discovery) {
            getCodecsForService(_ as String) >> { args -> ["application/json"] as String[] }
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
            sleep 200
            func(
                    MuonMessageBuilder.fromService("tombola")
                        .toService("simples")
                        .step("introspectionReport")
                        .protocol(IntrospectionServerProtocolStack.PROTOCOL)
                        .payload(codecs.encode(new ServiceExtendedDescriptor("tombola", [new ProtocolDescriptor("rrp", "Crazy Proto", "B happy", [])]), ["application/json"] as String[]).payload)
                        .contentType("application/json")
                        .buildInbound())
        }
        ServiceExtendedDescriptor descriptor = stack.introspect("simples").get()

        then:
        descriptor
        descriptor.serviceName == "tombola"
        descriptor.protocols.size() == 1
    }

  def "introspection of schemas"() {

    def func
    def codecs = new JsonOnlyCodecs()
    def discovery = Mock(Discovery) {
      getCodecsForService(_ as String) >> { args -> ["application/json"] as String[] }
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
      sleep 200
      def schema = new SchemasDescriptor("happy", "/", [:])
      func(
        MuonMessageBuilder.fromService("tombola")
          .toService("simples")
          .step("introspectionReport")
          .protocol(IntrospectionServerProtocolStack.PROTOCOL)
          .payload(codecs.encode(schema).payload)
          .contentType("application/json")
          .buildInbound())
    }

    SchemasDescriptor descriptor = stack.getSchemas("simples", "happy", "/").get()

    then:
    descriptor
    descriptor.protocol == "happy"
    descriptor.resource == "/"
    descriptor.schemas.size() == 0
  }
}
