package org.muoncore

import spock.lang.Specification
import java.lang.Void as Story

class MuonLifecycleSpec extends Specification {


  Story "start() initialises all extensions"() {
    expect:
    "Happy" == 1
  }

  Story "registering a resource adds it to the resource map"() {
    expect:
    "Happy" == 1
  }

  Story "adding a transport before start, it is not in the transport list"() {
    given:
    MuonEventTransport transport = Mock()
    Muon muon = new Muon()
    simpleGet(muon)

    muon.registerExtension(new MuonExtension() {
      @Override
      void init(MuonExtensionApi muonApi) {
        muonApi.addTransport(transport)
      }

      @Override
      String getName() {
        return "testextension/1.0"
      }
    })

    expect:
    muon.transports == []
  }

  Story "adding a transport call start(), all resources in the resource map are added"() {
    given:
    MuonEventTransport transport = Mock()
    Muon muon = new Muon()
    simpleGet(muon)

    muon.registerExtension(new MuonExtension() {
      @Override
      void init(MuonExtensionApi muonApi) {
        muonApi.addTransport(transport)
      }

      @Override
      String getName() {
        return "testextension/1.0"
      }
    })

    when:
    muon.start()


    then:
    1 * transport.listenOnResource("/simples", "get", _ as Muon.EventResourceTransportListener)
  }

  private simpleGet(Muon muon) {
    muon.resource("/simples", "Simple Documentation", new MuonService.MuonGet() {
      @Override
      Object onQuery(MuonResourceEvent queryEvent) {
        return null
      }
    })
  }
}
