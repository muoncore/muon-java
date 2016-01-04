package io.muoncore.discovery.multicast

import io.muoncore.ServiceDescriptor
import io.muoncore.transport.ServiceCache
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class MulticaseDiscoverySpec extends Specification {

    def "can fire up multiple multicast discoveries and have them interact"() {

        def disco1 = new MulticastDiscovery(new ServiceCache())
        def disco2 = new MulticastDiscovery(new ServiceCache())
        def disco3 = new MulticastDiscovery(new ServiceCache())

        when:
        disco1.advertiseLocalService(service("hello"))
        disco2.advertiseLocalService(service("tombola"))
        disco3.advertiseLocalService(service("simples"))

        then:
        new PollingConditions(timeout: 5).eventually {
            disco1.knownServices.size() == 3 &&
            disco2.knownServices.size() == 3 &&
            disco3.knownServices.size() == 3
        }
        disco1.knownServices.find { it.identifier == "simples"} != null
    }

    def service(String name) {
        new ServiceDescriptor(name, ["tag1"], ["text/json+AES"], [new URI("amqp://wibble")])

    }
}
