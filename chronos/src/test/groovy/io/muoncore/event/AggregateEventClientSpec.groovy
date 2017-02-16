package io.muoncore.event

import com.google.common.eventbus.EventBus
import io.muoncore.Chronos
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.event.client.AggregateEventClient
import io.muoncore.protocol.event.client.DefaultEventClient
import spock.lang.Specification
import java.lang.Void as Test

class AggregateEventClientSpec extends Specification {

  def eventbus = new EventBus()
  def discovery = new InMemDiscovery()

  Test "can store domain events and replay them"() {

    given:
    def muon = muon("events")
    def chronos = eventStore(muon)
    def dddClient = new AggregateEventClient(new DefaultEventClient(muon))

    when:
    dddClient.publishDomainEvents("1234", [
            [msg:"happy"],
            [msg:"happy2"],
            [msg:"happy3"],
            [msg:"happy4"],
            [msg:"happy5"]
    ])

    then:
    dddClient.loadAggregateRoot("1234").collect {it.getPayload(Map)} == [
      [msg:"happy"],
      [msg:"happy2"],
      [msg:"happy3"],
      [msg:"happy4"],
      [msg:"happy5"]
    ]
  }

  Muon muon(name) {
    def config = new AutoConfiguration(serviceName: name)
    config.setTags(["eventstore"])
    def transport = new InMemTransport(config, eventbus)

    new MultiTransportMuon(config, discovery, [transport], new JsonOnlyCodecs())
  }

  def eventStore(Muon muon) {
    return new Chronos(muon)
  }
}
