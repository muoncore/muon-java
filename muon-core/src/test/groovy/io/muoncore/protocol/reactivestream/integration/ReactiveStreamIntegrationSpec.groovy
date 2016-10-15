package io.muoncore.protocol.reactivestream.integration
import com.google.common.eventbus.EventBus
import io.muoncore.Muon
import io.muoncore.MultiTransportMuon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.config.AutoConfiguration
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.memory.transport.InMemTransport
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ReactiveStreamIntegrationSpec extends Specification {

    def discovery = new InMemDiscovery()
    def eventbus = new EventBus()

    def "can create a publisher and subscribe to it remotely"() {

        Environment.initializeIfEmpty()

        def data = []

        def b = Broadcaster.create()
        def sub2 = Broadcaster.create()

        sub2.consume {
            data << it
        }

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        muon1.publishSource("somedata", PublisherLookup.PublisherType.HOT, b)

        when:
        muon2.subscribe(new URI("stream://simples/somedata"), Map, sub2)

        sleep(100)

        and:
        5000.times {
            b.accept(["hello": "world"])
        }

        then:
        new PollingConditions(timeout: 20).eventually {
            data.size() == 5000
        }
    }

    @Ignore
    def "subscribing to remote fails with onError"() {

        def data = []
        def errorReceived = false

        Environment env = Environment.initializeIfEmpty()

        def sub2 = Broadcaster.create(env)
        sub2.observeError(Exception, {
            println "ERROR WAS FOUND"
            errorReceived = true
        }).consume {
            println "BAD JUJU"
        }

        sub2.consume {
            println "SOmething good?"
            data << it
        }

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        when:
        muon2.subscribe(new URI("stream://simples/BADSTREAM"), Map, sub2)

        then:
        new PollingConditions().eventually {
            errorReceived
        }
    }

    def "data remains in order"() {

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        StandardAsyncChannel.echoOut = true
        def env = Environment.initializeIfEmpty()

        def data = []

        def b = Broadcaster.create(env)
        def sub2 = Broadcaster.create(env)

        sub2.consume {
            data << it
        }

        muon1.publishSource("somedata", PublisherLookup.PublisherType.HOT, b)

        sleep(4000)
        when:
        muon2.subscribe(new URI("stream://simples/somedata"), Integer, sub2)

        sleep(1000)

        def inc = 1

        and:
        200.times {
            println "Publish"
            b.accept(inc++)
        }
        sleep(5000)

        def sorted = new ArrayList<>(data).sort()

        then:

        data == sorted

        cleanup:
        StandardAsyncChannel.echoOut = false
    }

    def "client receives complete signal correctly"() {

        def muon1 = muon("simples")
        def muon2 = muon("tombola")

        StandardAsyncChannel.echoOut = true
        def env = Environment.initializeIfEmpty()

        def data = []

        Publisher pub = new Publisher() {
            @Override
            void subscribe(Subscriber s) {
                s.onSubscribe(new Subscription() {
                    @Override
                    void request(long n) {
                        println "CLIENT REQUESTED DATA $n"
                    }

                    @Override
                    void cancel() {
                        println "CLIENT SENT CANCEL"
                    }
                })
                Thread.start{
                    sleep 500
                    s.onComplete()
                }
            }
        }

        def completed = false

        Subscriber sub = new Subscriber() {
            @Override
            void onSubscribe(Subscription s) {
                println "Received SUBSCRIBE"
            }

            @Override
            void onNext(Object o) {
                println "Received NEXT"
            }

            @Override
            void onError(Throwable t) {
                println "Received ERROR"
            }

            @Override
            void onComplete() {
                println "Received COMPLETE"
                completed=true
            }
        }

        muon1.publishSource("somedata", PublisherLookup.PublisherType.HOT, pub)

        sleep 500
        when:
        muon2.subscribe(new URI("stream://simples/somedata"), Integer, sub)

        then:

        new PollingConditions().eventually {
            completed == true
        }

        cleanup:
        StandardAsyncChannel.echoOut = false
    }


    Muon muon(name) {
        def config = new AutoConfiguration(serviceName: name)
        def transport = new InMemTransport(config, eventbus)

        new MultiTransportMuon(config, discovery, [transport])
    }
}
