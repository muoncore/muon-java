package io.muoncore.transport

import io.muoncore.ServiceDescriptor
import io.muoncore.transport.support.TransportList
import spock.lang.Specification

class TransportListSpec extends Specification {

  def "transport list returns the correct transport for a remote descriptor"() {

    given:
    def list = new TransportList<MuonEventTransport>()

    def amqp = Mock(MuonEventTransport) {
      getUrlScheme() >> "amqp"
    }
    def zeromq = Mock(MuonEventTransport) {
      getUrlScheme() >> "io.muoncore.extension.zeromq"
    }
    list.addTransport(zeromq)
    list.addTransport(amqp)

    expect:
    list.findBestResourceTransport(desc()) == amqp
  }

  ServiceDescriptor desc() {
    new ServiceDescriptor(
        "my-service",
        [],
        [
            new URI("http://simple"),
            new URI("amqp://broker:8181"),
        ],
        [
            new URI("amqp://broker:8181"),
        ]
    )
  }
}
