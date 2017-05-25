package io.muoncore.discovery

import io.muoncore.Discovery
import io.muoncore.InstanceDescriptor
import io.muoncore.ServiceDescriptor
import spock.lang.Specification

class MultiDiscoverySpec extends Specification {

   def "creates aggregate list of services from getServiceNames()"(){


       def mock1 = Mock(Discovery) {
           getServiceNames() >> ["tombola", "simples"]
       }
       def mock2 = Mock(Discovery) {
         getServiceNames() >>["tombola", "crazy"]
       }
       def mock3 = Mock(Discovery) {
         getServiceNames() >> ["tombola", "bangle"]
       }

       def discovery = new MultiDiscovery([mock1, mock2, mock3])

       when:
       def services = discovery.serviceNames

       then:
       services.size() == 4
    }

    def "finds a service from wrapped discos"() {
      def mock1 = Mock(Discovery) {
        getServiceNamed("tombola") >> Optional.of(ident("tombola"))
      }
      def mock2 = Mock(Discovery) {
        getServiceNamed(_) >> Optional.empty()
      }

      def discovery = new MultiDiscovery([mock1, mock2])

      expect:
      discovery.getServiceNamed("tombola").get().identifier == "tombola"
    }

    def "finds a service using tags from wrapped discos"() {
      def mock1 = Mock(Discovery) {
        getServiceWithTags("hello", "world") >> Optional.of(ident("tombola"))
      }
      def mock2 = Mock(Discovery) {
        getServiceWithTags("hello", "world") >> Optional.empty()
      }

      def discovery = new MultiDiscovery([mock1, mock2])

      expect:
      discovery.getServiceWithTags("hello", "world").get().identifier == "tombola"
    }

    def "advertiseLocalService calls advertise on all delegates"() {
        def mock1 = Mock(Discovery)
        def mock2 = Mock(Discovery)
        def mock3 = Mock(Discovery)

        def discovery = new MultiDiscovery([mock1, mock2, mock3])

        def ident = new InstanceDescriptor("123", "ident",[],[],[], [])

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
