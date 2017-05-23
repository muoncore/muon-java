package io.muoncore.extension.amqp.discovery

import io.muoncore.InstanceDescriptor
import io.muoncore.ServiceDescriptor
import io.muoncore.codec.Codecs
import io.muoncore.extension.amqp.AmqpConnection
import io.muoncore.extension.amqp.QueueListener
import io.muoncore.extension.amqp.QueueListenerFactory
import io.muoncore.transport.ServiceCache
import spock.lang.Specification

class AmqpDiscoverySpec extends Specification {

    def "service cache data is converted into ServiceDescriptors"() {
        def cache = Mock(ServiceCache) {
            getServices() >> [new ServiceDescriptor("tombola", ["tag1"], ["application/json", "application/json+AES"], [], [
              new InstanceDescriptor("12345", "tombola", ["tag1"], ["application/json", "application/json+AES"], [new URI("amqp://hello")], [])])]
        }
        def listenerFactory = Mock(QueueListenerFactory)
        def connection = Mock(AmqpConnection)
        def codecs = Mock(Codecs)

        def discovery = new AmqpDiscovery(listenerFactory, connection, cache, codecs)

        when:
        def services = discovery.serviceNames.collect {
          discovery.getServiceNamed(it).get()
        }

        then:
        services.size() == 1
        services[0].identifier == "tombola"
        services[0].codecs == ["application/json", "application/json+AES"] as String[]
        services[0].tags == ["tag1"]
    }

    def "amqp discovery broadcasts every 3 seconds"() {
        def cache = Mock(ServiceCache) {
            getServices() >> [new InstanceDescriptor("12", "tombola", ["tag1"], ["application/json", "application/json+AES"], [new URI("amqp://hello")], [])]
        }
        QueueListenerFactory listenerFactory = Mock(QueueListenerFactory)
        AmqpConnection connection = Mock(AmqpConnection)
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult(new byte[0], "application/json")
        }

        def discovery = new AmqpDiscovery(listenerFactory, connection, cache, codecs)

        when:
        discovery.start()
        discovery.advertiseLocalService(new InstanceDescriptor("12", "tombola", ["simples"], ["application/json"], [new URI("amqp://nothing")], []))
        sleep(4000)

        then:
        1 * connection.broadcast({ QueueListener.QueueMessage msg ->
            msg.queueName == "discovery"
        } as QueueListener.QueueMessage)
    }

    def "reconnects when remote broker drops"() {



    }
}
