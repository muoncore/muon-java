package io.muoncore.protocol.reactivestream.server

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.protocol.reactivestream.ProtocolMessages
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification

class ReactiveStreamServerChannelSpec extends Specification {


    //if publisher exists, send ACK and connect the channel to it.

    //if publisher doesn't exist, send NACK down the channel and close it.

    //once ACK established.

    def "sends ACK if the publisher does exist on SUBSCRIBE"() {
        def subscription = Mock(Subscription)
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                args[0].onSubscribe(subscription)
            }
        }

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        then: "NACK sent back"
        1 * function.apply({ TransportMessage msg ->
            msg.type == ProtocolMessages.ACK
        })
    }


    def "sends NACK if the publisher doesn't exist on SUBSCRIBE"() {

        def publookup = Mock(PublisherLookup) {
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        then: "NACK sent back"
        1 * function.apply({ TransportMessage msg ->
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

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        channel.send(new TransportInboundMessage(
                ProtocolMessages.REQUEST,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [request:"100"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

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

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        channel.send(new TransportInboundMessage(
                ProtocolMessages.CANCEL,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

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

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        and: "subscriber.onNext is called"
        subscriber.onNext([simple:"message"])

        then:
        1 * function.apply({ TransportMessage msg ->
            msg.type == ProtocolMessages.DATA
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

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        and: "subscriber.onNext is called"
        subscriber.onComplete()

        then:
        1 * function.apply({ TransportMessage msg ->
            msg.type == ProtocolMessages.COMPLETE
        })
    }

    def "calling onError causes a ERROR message to dispatch"() {
        Subscriber subscriber
        def pub = Mock(Publisher) {
            subscribe(_) >> { args ->
                subscriber = args[0]
            }
        }

        def publookup = Mock(PublisherLookup) {
            lookupPublisher("simpleStream") >> Optional.of(pub)
            lookupPublisher(_) >> Optional.empty()
        }
        def function = Mock(ChannelConnection.ChannelFunction)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
            encode(_, _) >> new Codecs.EncodingResult([] as byte[], "application/json")
        }

        def channel = new ReactiveStreamServerChannel(publookup, codecs)
        channel.receive(function)

        when: "SUBSCRIBE from client"
        channel.send(new TransportInboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [streamName:"simpleStream"],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        and: "subscriber.onError is called"
        subscriber.onError(new IllegalStateException("Messed up"))

        then:
        1 * function.apply({ TransportMessage msg ->
            msg.type == ProtocolMessages.ERROR
        })
    }

    the service names aren't set correctly.' +

    need to ensure the exception is correctly broken down somehow and propogated in a portable way.


}
