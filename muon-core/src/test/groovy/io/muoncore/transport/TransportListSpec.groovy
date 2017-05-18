package io.muoncore.transport

import io.muoncore.InstanceDescriptor
import io.muoncore.ServiceDescriptor
import spock.lang.Specification

class TransportListSpec extends Specification {

  def "transport list returns the correct transport for a remote descriptor"() {

    given:
    def list = new TransportList<MuonTransport>()

    def amqp = Mock(MuonTransport) {
      getUrlScheme() >> "amqp"
    }
    def zeromq = Mock(MuonTransport) {
      getUrlScheme() >> "io.muoncore.extension.zeromq"
    }
    list.addTransport(zeromq)
    list.addTransport(amqp)

    expect:
    list.findBestTransport(desc()) == amqp
  }

  ServiceDescriptor desc() {
    new ServiceDescriptor(
        "my-service",
        [], [], [],
        [
          new InstanceDescriptor("","", [], [], [new URI("http://simple")], []),
          new InstanceDescriptor("","", [], [], [new URI("amqp://broker")], [])
        ]
    )
  }
}
