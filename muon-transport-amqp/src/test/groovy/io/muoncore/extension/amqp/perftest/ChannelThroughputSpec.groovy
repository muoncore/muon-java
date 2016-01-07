package io.muoncore.extension.amqp.perftest
import io.muoncore.ServiceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.GsonCodec
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.protocol.ServerStacks
import io.muoncore.protocol.requestresponse.RRPTransformers
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportOutboundMessage
import reactor.Environment
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@IgnoreIf({ System.getenv("BUILD_NUMBER") })
class ChannelThroughputSpec extends Specification {

    def discovery = new InMemDiscovery()
    def serverStacks1 = Mock(ServerStacks)

    @Unroll
    def "can establish #numservices channels and send #numRequests messages"() {

        Environment.initializeIfEmpty()

        AMQPMuonTransport svc1 = createTransport("service1")

        def received = []

        def stacks = new ServerStacks() {
            @Override
            ChannelConnection<TransportInboundMessage, TransportOutboundMessage> openServerChannel(String protocol) {
                println "Opening channel for proto $protocol"
                return new ChannelConnection<TransportInboundMessage, TransportOutboundMessage>() {
                    @Override
                    void receive(ChannelConnection.ChannelFunction<TransportOutboundMessage> function) {
                        println "eh?"
                    }

                    @Override
                    void send(TransportInboundMessage message) {
                        received << message
                    }

                    @Override
                    void shutdown() {
                        println "SHUTDOWN!"
                    }
                }
            }
        }

        AMQPMuonTransport svc2 = createTransport("tombola")

        svc2.start(discovery, stacks)
        svc1.start(discovery, serverStacks1)

        discovery.advertiseLocalService(new ServiceDescriptor("tombola", [], [], []))
        discovery.advertiseLocalService(new ServiceDescriptor("service1", [], [], []))

        sleep(4000)

        when:
        def pool = Executors.newFixedThreadPool(20)

        def id = new AtomicInteger()

        numservices.times {

            def channel = svc1.openClientChannel("tombola", "requestresponse")

            numRequests.times {
                pool.submit {
                println "Sending data .."
                channel.send(new TransportOutboundMessage(
                        "somethingHappened",
                        "${id.addAndGet(1)}",
                        "tombola",
                        "service1",
                        RRPTransformers.REQUEST_RESPONSE_PROTOCOL,
                        [:],
                        "application/json",
                        new GsonCodec().encode([:]), ["application/json"]))
                }
            }
        }
        sleep(5000)

        pool.shutdown()

        then:
        new PollingConditions(timeout: 30).eventually {
            try {
                new ArrayList<>(received).metadata.id.size() == numRequests * numservices
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }

        cleanup:
        svc1.shutdown()
        svc2.shutdown()

        where:
        numservices | numRequests
        5   | 5
        5  | 30
//        2  | 200
//        1  | 10000
        20  | 1000
    }

    private AMQPMuonTransport createTransport(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new RabbitMq09QueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)
        svc1
    }
}
