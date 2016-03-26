package io.muoncore.transport.client
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class SimpleTransportMessageDispatcherSpec extends Specification {

    def "distributes data to a listener"() {

        given:

        def data = []

        def dispatcher = new SimpleTransportMessageDispatcher()
        dispatcher.observe({ true }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })

        when:
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.shutdown()

        then:
        new PollingConditions().eventually {
            data.size() == 5
        }
    }

    def "triggers onComplete whenn a shutdown signal is received"() {

        given:

        def data = []
        def complete = false

        def dispatcher = new SimpleTransportMessageDispatcher()
        dispatcher.observe({ true }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {
                complete = true
            }
        })

        when:
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.shutdown()

        then:
        new PollingConditions().eventually {
            complete
        }
    }

    def "distributes data to multiple listener"() {

        given:

        def data = []

        def dispatcher = new SimpleTransportMessageDispatcher()
        dispatcher.observe({ true }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })
        dispatcher.observe({ true }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })
        dispatcher.observe({ true }).subscribe(new Subscriber<TransportMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(TransportMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })
        sleep(500)

        when:
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())
        dispatcher.dispatch(inbound())

        then:
        new PollingConditions(timeout: 5).eventually {
            data.size() == 15
        }

        cleanup:
        dispatcher.shutdown()
    }

    def inbound() {
        new TransportInboundMessage(
                "mydata",
                "faked",
                "myTarget",
                "mySource",
                "streamish",
                [:],
                "application/json+AES",
                [] as byte[],
                [], TransportMessage.ChannelOperation.NORMAL
        )
    }
}
