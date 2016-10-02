package io.muoncore.discovery

import io.muoncore.Discovery
import io.muoncore.ServiceDescriptor
import spock.lang.Specification

class MultiDiscoverySpec extends Specification {

   def "creates aggregate list of services from getKnownServices()"(){


       def mock1 = Mock(Discovery) {
           getKnownServices() >> [ident("tombola"), ident("simples")]
       }
       def mock2 = Mock(Discovery) {
           getKnownServices() >> [ident("tombola"), ident("crazy")]
       }
       def mock3 = Mock(Discovery) {
           getKnownServices() >> [ident("tombola"), ident("bangle")]
       }

       def discovery = new MultiDiscovery([mock1, mock2, mock3])

       when:
       def services = discovery.knownServices

       then:
       services.size() == 4
    }

    def "advertiseLocalService calls advertise on all delegates"() {
        def mock1 = Mock(Discovery)
        def mock2 = Mock(Discovery)
        def mock3 = Mock(Discovery)

        def discovery = new MultiDiscovery([mock1, mock2, mock3])

        def ident = new ServiceDescriptor("ident",[],[],[], [])

        when:
        discovery.advertiseLocalService(ident)

        then:
        1 * mock1.advertiseLocalService(ident)
        1 * mock2.advertiseLocalService(ident)
        1 * mock3.advertiseLocalService(ident)
    }

    def "onReady is undefined...?"() {


    }


    def "shutdown invokes all"() {

        def mock1 = Mock(Discovery)
        def mock2 = Mock(Discovery)
        def mock3 = Mock(Discovery)

        def discovery = new MultiDiscovery([mock1, mock2, mock3])

        when:
        discovery.shutdown()

        then:
        1 * mock1.shutdown()
        1 * mock2.shutdown()
        1 * mock3.shutdown()

    }

    def ident(name) {
        new ServiceDescriptor(name,[],[],[], [])
    }

}
