package io.muoncore.protocol.introspection.server

import io.muoncore.Discovery
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.descriptors.SchemasDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.descriptors.ServiceExtendedDescriptorSource
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.introspection.SchemaIntrospectionRequest
import io.muoncore.protocol.introspection.client.IntrospectClientProtocol
import io.muoncore.protocol.introspection.client.IntrospectSchemasClientProtocol
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class IntrospectionServerProtocolStackSpec extends Specification {

  def discovery = Mock(Discovery)

  def "responds with an introspection report"() {
    def descriptorSource = Mock(ServiceExtendedDescriptorSource) {
      getServiceExtendedDescriptor() >> new ServiceExtendedDescriptor("awesome", [])
    }
    def codecs = new JsonOnlyCodecs()

    def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs, discovery)

    MuonOutboundMessage outbound

    def channel = stack.createChannel()
    channel.receive({
      if (it != null) outbound = it
    })

    when:
    channel.send(
      MuonMessageBuilder
        .fromService("tombola")
        .toService("simples")
        .step(IntrospectClientProtocol.INTROSPECTION_REQUESTED)
        .protocol(IntrospectionServerProtocolStack.PROTOCOL)
        .contentType("application/json")
        .payload()
        .buildInbound())

    then:
    new PollingConditions().eventually {
      outbound &&
        codecs.decode(outbound.payload, "application/json", Map).serviceName == "awesome"
    }
  }

  def "responds with a schema introspection report"() {
    def descriptorSource = Mock(ServiceExtendedDescriptorSource) {
      getSchemasDescriptor({
        it.protocol == "happy" && it.endpoint == "/"
      }) >> new SchemasDescriptor("happy", "/", [:])
    }
    def codecs = new JsonOnlyCodecs()

    def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs, discovery)

    MuonOutboundMessage outbound

    def channel = stack.createChannel()
    channel.receive({
      if (it != null) outbound = it
    })

    when:
    channel.send(
      MuonMessageBuilder
        .fromService("tombola")
        .toService("simples")
        .step(IntrospectSchemasClientProtocol.SCHEMA_INTROSPECTION_REQUESTED)
        .protocol(IntrospectionServerProtocolStack.PROTOCOL)
        .contentType("application/json")
        .payload(codecs.encode(new SchemaIntrospectionRequest("happy", "/")).payload)
        .buildInbound())

    then:
    new PollingConditions().eventually {
      outbound &&
        codecs.decode(outbound.payload, "application/json", Map).protocol == "happy"
      codecs.decode(outbound.payload, "application/json", Map).resource == "/"
    }
  }

  def "generates protocol descriptor"() {

    def descriptorSource = Mock(ServiceExtendedDescriptorSource)
    def codecs = Mock(Codecs)

    def stack = new IntrospectionServerProtocolStack(descriptorSource, codecs, discovery)

    expect:
    stack.protocolDescriptor.operations.size() == 0
    stack.protocolDescriptor.protocolScheme == "introspect"
  }
}
