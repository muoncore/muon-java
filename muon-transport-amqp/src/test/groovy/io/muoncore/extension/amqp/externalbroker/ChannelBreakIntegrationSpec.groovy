package io.muoncore.extension.amqp.externalbroker

import com.rabbitmq.client.Channel
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.AMQPMuonTransport
import io.muoncore.extension.amqp.BaseEmbeddedBrokerSpec
import io.muoncore.extension.amqp.DefaultAmqpChannelFactory
import io.muoncore.extension.amqp.DefaultServiceQueue
import io.muoncore.extension.amqp.QueueListener
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.message.MuonMessage
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@IgnoreIf({ System.getenv("SHORT_TEST") })
class ChannelBreakIntegrationSpec extends BaseEmbeddedBrokerSpec {

    @Shared def discovery = new InMemDiscovery()

    @AutoCleanup("shutdown")
    @Shared
    def muon1
    @Shared
    ControllableQueueListenerFactory queuefactory1
    @Shared
    ControllableQueueListenerFactory queuefactory2

    @AutoCleanup("shutdown")
    @Shared
    Muon muon2

    def setupSpec() {
        def l
        (queuefactory1, muon1) = muon("simples")
        (queuefactory2, muon2) = muon("tombola")
    }

    def "reactive-stream subscribing to a remote, then kill remote, causes onError"() {

      StandardAsyncChannel.echoOut = true
        def env = Environment.initializeIfEmpty()

        def data = []

        def error
        def b = Broadcaster.create(env)
        def sub2 = new Subscriber() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(5000)
            }

            @Override
            void onNext(Object o) {
              println "$o"
                data << o
            }

            @Override
            void onError(Throwable t) {
                println "ERROR HAPPENED"
                t.printStackTrace()
                error = t
            }

            @Override
            void onComplete() {
                println ("Completed stream")
            }
        }

      Broadcaster tap = Broadcaster.create()
      tap.consume {
        println "MSG ${it}"
      }
      muon2.getTransportControl().tap({ true }).subscribe(tap)

        muon1.publishSource("somedata", PublisherLookup.PublisherType.HOT, b)
        sleep(4000)
        when:
        muon2.subscribe(new URI("stream://simples/somedata"), sub2)

        sleep(1000)

        and:
        Thread.start {
          20.times {
            sleep (500)
            println "Publish"
            b.accept(["hello": "world"])
          }
        }
        sleep(100)
//        queuefactory1.simulateRemoteFailure()
        queuefactory2.simulateRemoteFailure()

        sleep(5000)
        then:

        new PollingConditions(timeout: 20).eventually {
            error != null
        }

        cleanup:
        StandardAsyncChannel.echoOut = false
    }

    private def muon(serviceName) {

        def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
        def queueFactory = new ControllableQueueListenerFactory(connection.channel)
        def serviceQueue = new DefaultServiceQueue(serviceName, connection)
        def channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection)

        def svc1 = new AMQPMuonTransport(
                "amqp://muon:microservices@localhost", serviceQueue, channelFactory)

        def config = new AutoConfiguration(serviceName:serviceName)
        def muon = new MultiTransportMuon(config, discovery, [svc1], new JsonOnlyCodecs())

        return [queueFactory, muon]
    }

    class ControllableQueueListenerFactory extends RabbitMq09QueueListenerFactory {
        def running = true
        ControllableQueueListenerFactory(Channel channel) {
            super(channel)
        }

        @Override
        QueueListener listenOnQueue(String queueName, QueueListener.QueueFunction function) {
            def func = {
                if (running) {
                    function.exec(it)
                } else {
                    println "Dropping message due to simulated failure"
                }
            } as QueueListener.QueueFunction
            return super.listenOnQueue(queueName, func)
        }

        public void simulateRemoteFailure() {
            println "Simulating remote service connection failure"
            running = false
        }
    }
}
