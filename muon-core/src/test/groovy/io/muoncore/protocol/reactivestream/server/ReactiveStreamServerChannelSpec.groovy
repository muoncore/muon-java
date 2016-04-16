package io.muoncore.protocol.reactivestream.server

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.protocol.reactivestream.ProtocolMessages
import io.muoncore.message.MuonInboundMessage
import io.muoncore.message.MuonMessage
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification

class ReactiveStreamServerChannelSpec extends Specification {

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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        then: "NACK sent back"
        1 * function.apply({ MuonMessage msg ->
            msg.type == ProtocolMessages.ACK
        })
    }


    def "sends NACK if the publisher doesn't exist on SUBSCRIBE"() {

        def publookup = Mock(PublisherLookup) {
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def config = new AutoConfiguration(serviceName: "awesome")
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        then: "NACK sent back"
        1 * function.apply({ MuonMessage msg ->
            msg.type == ProtocolMessages.NACK
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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        channel.send(new MuonInboundMessage(
                ProtocolMessages.REQUEST,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [request:"100"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        channel.send(new MuonInboundMessage(
                ProtocolMessages.CANCEL,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        and: "subscriber.onNext is called"
        subscriber.onNext([simple:"message"])

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.type == ProtocolMessages.DATA &&
                    msg.channelOperation == MuonMessage.ChannelOperation.NORMAL &&
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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        and: "subscriber.onNext is called"
        subscriber.onComplete()

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.type == ProtocolMessages.COMPLETE &&
                    msg.channelOperation == MuonMessage.ChannelOperation.CLOSE_CHANNEL &&
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
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs, config)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new MuonInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                MuonMessage.ChannelOperation.NORMAL))

        and: "subscriber.onError is called"
        subscriber.onError(new IllegalStateException("Messed up"))

        then:
        1 * function.apply({ MuonMessage msg ->
            msg.type == ProtocolMessages.ERROR &&
            msg.channelOperation == MuonMessage.ChannelOperation.CLOSE_CHANNEL &&
                    msg.targetServiceName == "tombola"
        })
    }
}
