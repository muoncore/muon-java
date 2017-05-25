package io.muoncore.extension.amqp.perftest

import io.muoncore.InstanceDescriptor
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.support.Scheduler
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.BaseEmbeddedBrokerSpec
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.ServerStacks
import reactor.Environment
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@IgnoreIf({ System.getenv("SHORT_TEST") })
class ChannelThroughputSpec extends BaseEmbeddedBrokerSpec {

    def discovery = new InMemDiscovery()

    @AutoCleanup("shutdown")
    Scheduler scheduler = new Scheduler()

    @Unroll
    def "can establish #numservices channels and send #numRequests messages"() {

        Environment.initializeIfEmpty()

        AMQPMuonTransport service1 = createTransport("service1")

        def received = []

        def stacks = new ServerStacks() {
            @Override
            ChannelConnection<MuonInboundMessage, MuonOutboundMessage> openServerChannel(String protocol) {
                println "Opening channel for proto $protocol"
                return new ChannelConnection<MuonInboundMessage, MuonOutboundMessage>() {
                    @Override
                    void receive(ChannelConnection.ChannelFunction<MuonOutboundMessage> function) {
                        println "eh?"
                    }

                    @Override
                    void send(MuonInboundMessage message) {
                        received << message
                    }

                    @Override
                    void shutdown() {
                        println "SHUTDOWN!"
                    }
                }
            }
        }
        def stacks2 = new ServerStacks() {
            @Override
            ChannelConnection<MuonInboundMessage, MuonOutboundMessage> openServerChannel(String protocol) {
                println "SERVICE1 Opening channel for proto $protocol"
                return new ChannelConnection<MuonInboundMessage, MuonOutboundMessage>() {
                    @Override
                    void receive(ChannelConnection.ChannelFunction<MuonOutboundMessage> function) {
                        println "eh?"
                    }

                    @Override
                    void send(MuonInboundMessage message) {
                        received << message
                    }

                    @Override
                    void shutdown() {
                        println "SHUTDOWN!"
                    }
                }
            }
        }

        AMQPMuonTransport tombola = createTransport("tombola")

        tombola.start(discovery, stacks, new JsonOnlyCodecs(), scheduler)
        service1.start(discovery, stacks2, new JsonOnlyCodecs(), scheduler)

        discovery.advertiseLocalService(new InstanceDescriptor("1","tombola", [], [], [], []))
        discovery.advertiseLocalService(new InstanceDescriptor("321","service1", [], [], [], []))

        sleep(4000)

        when:
        def pool = Executors.newFixedThreadPool(20)

        def id = new AtomicInteger()

        numservices.times {

            def channel = service1.openClientChannel("tombola", "requestresponse")
            channel.receive({
                println "Hello ${it}"
            })
            def counterLatch = new CountDownLatch(numRequests)

            numRequests.times {
                pool.submit {
                    println "Sending data .."

                    channel.send(
                            MuonMessageBuilder
                                    .fromService("service1")
                                    .step("somethingHappened")
                                    .protocol(RRPTransformers.REQUEST_RESPONSE_PROTOCOL)
                                    .toService("tombola")
                                    .payload([] as byte[])
                                    .contentType("application/json")
                                    .build())
                    counterLatch.countDown()
                }

            }
            counterLatch.await()
            channel.shutdown()
        }
        sleep(5000)

        pool.shutdown()

        then:
        new PollingConditions(timeout: 60).eventually {
            try {
                new ArrayList<>(received)?.id?.size() >= numRequests * numservices
            } catch (Exception ex) {
                ex.printStackTrace()
            }
        }

        cleanup:
        service1.shutdown()
        tombola.shutdown()

        where:
        numservices | numRequests
        5   | 5
        5  | 30
        2  | 200
        1  | 10000
        20  | 1000
        200  | 2000
        4000  | 3
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
