package io.muoncore.transport.client

import io.muoncore.codec.json.GsonCodec
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class SimpleTransportMessageDispatcherSpec extends Specification {

    def "distributes data to a listener"() {

        given:

        def data = []

        def dispatcher = new SimpleTransportMessageDispatcher()
        dispatcher.observe({ true }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
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
        dispatcher.observe({ true }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
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

        def data = Collections.synchronizedList([])

        def dispatcher = new SimpleTransportMessageDispatcher()
        dispatcher.observe({ true }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })
        dispatcher.observe({ true }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
                println "Got data"
                data << transportMessage
            }

            @Override
            void onError(Throwable t) {}

            @Override
            void onComplete() {}
        })
        dispatcher.observe({ true }).subscribe(new Subscriber<MuonMessage>() {
            @Override
            void onSubscribe(Subscription s) {
                s.request(100)
            }

            @Override
            void onNext(MuonMessage transportMessage) {
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
        MuonMessageBuilder.fromService("mySource")
            .toService("myTarget")
            .step("faked")
            .protocol("streamish")
            .contentType("application/json")
            .payload(new GsonCodec().encode([:]))
            .buildInbound()
    }
}
