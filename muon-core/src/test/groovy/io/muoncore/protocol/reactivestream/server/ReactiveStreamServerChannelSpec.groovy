package io.muoncore.protocol.reactivestream.server

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.protocol.reactivestream.ProtocolMessages
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.messages.RequestMessage
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification

class ReactiveStreamServerChannelSpec extends Specification {

    def discovery = Mock(Discovery) {
        getCodecsForService(_) >> { ["application/json"] as String[] }
    }
    def codecs = new JsonOnlyCodecs()

    def "sends ACK if the publisher does exist on SUBSCRIBE"() {
        def subscription = Mock(Subscription)
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                args[0].onSubscribe(subscription)
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(
                    new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.SUBSCRIBE)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                        .buildInbound())

        then: "NACK sent back"
        1 * function.apply({ MuonMessage msg ->
            msg.step == ProtocolMessages.ACK
        })
    }


    def "sends NACK if the publisher doesn't exist on SUBSCRIBE"() {

        def publookup = Mock(PublisherLookup) {
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def config = new AutoConfiguration(serviceName: "awesome")

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(MuonMessageBuilder
                .fromService("tombola")
                .toService("awesome")
                .step(ProtocolMessages.SUBSCRIBE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                .buildInbound())

        then: "NACK sent back"
        1 * function.apply({ MuonMessage msg ->
            msg.step == ProtocolMessages.NACK
        })
    }

    def "on receive REQUEST call subscription.request"() {
        def subscription = Mock(Subscription)
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                args[0].onSubscribe(subscription)
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(MuonMessageBuilder
                .fromService("tombola")
                .toService("awesome")
                .step(ProtocolMessages.SUBSCRIBE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                .buildInbound())

        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.REQUEST)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode(new RequestMessage(100)))
                        .buildInbound())

        then:
        1 * subscription.request(100)
    }

    def "on receive CANCEL call subscription.cancel"() {
        def subscription = Mock(Subscription)
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                args[0].onSubscribe(subscription)
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.SUBSCRIBE)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                        .buildInbound())

        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.CANCEL)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

        then:
        1 * subscription.cancel()
    }

    def "calling subscriber onNext causes a DATA message to dispatch"() {
        Subscriber subscriber
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                subscriber = args[0]
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(
                MuonMessageBuilder
                .fromService("tombola")
                .toService("awesome")
                .step(ProtocolMessages.SUBSCRIBE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                .buildInbound())

        and: "subscriber.onNext is called"
        subscriber.onNext([simple:"message"])

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.step == ProtocolMessages.DATA &&
                    msg.channelOperation == MuonMessage.ChannelOperation.normal &&
                    msg.targetServiceName == "tombola"
        })
        //TODO, verify data/ codec usage
    }

    def "calling onComplete causes a COMPLETE message to dispatch"() {
        Subscriber subscriber
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                subscriber = args[0]
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.SUBSCRIBE)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                        .buildInbound())

        and: "subscriber.onNext is called"
        subscriber.onComplete()

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.step == ProtocolMessages.COMPLETE &&
                    msg.channelOperation == MuonMessage.ChannelOperation.closed &&
                    msg.targetServiceName == "tombola"
        })
    }

    def "calling onError causes a ERROR message to dispatch"() {
        Subscriber subscriber
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                subscriber = args[0]
            }
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(new PublisherLookup.PublisherRecord("simpleStream", PublisherLookup.PublisherType.HOT, new ImmediatePublisherGenerator(pub)))
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config, discovery)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.SUBSCRIBE)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode(new ReactiveStreamSubscriptionRequest("simpleStream")))
                        .buildInbound())

        and: "subscriber.onError is called"
        subscriber.onError(new IllegalStateException("Messed up"))

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.step == ProtocolMessages.ERROR &&
            msg.channelOperation == MuonMessage.ChannelOperation.closed &&
                    msg.targetServiceName == "tombola"
        })
    }
}
