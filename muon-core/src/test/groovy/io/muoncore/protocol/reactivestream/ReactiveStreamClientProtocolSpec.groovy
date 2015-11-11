package io.muoncore.protocol.reactivestream

import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.exception.MuonException
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocol
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerStack
import io.muoncore.transport.TransportInboundMessage
import io.muoncore.transport.TransportMessage
import io.muoncore.transport.TransportOutboundMessage
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification

class ReactiveStreamClientProtocolSpec extends Specification {

    def "sends SUBSCRIBE when the proto is started"() {

        def uri = new URI("")
        def connection = Mock(ChannelConnection)
        def sub = Mock(Subscriber)
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        then:
        1 * connection.send({ TransportOutboundMessage msg ->
            msg.channelOperation == TransportMessage.ChannelOperation.NORMAL &&
                    msg.protocol == ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL &&
                    msg.type == ProtocolMessages.SUBSCRIBE
        })

        and: "Subscriber is unused"
        0 * sub._
    }

    def "on ACK received, calls subscriber.onSubscribe"() {

        def function

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber)
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()
        function(new TransportInboundMessage(
                ProtocolMessages.ACK,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        sleep(50)

        then:
        1 * sub.onSubscribe(_)
    }

    def "on NACK received, calls subscriber.onSubscribe and subscriber error"() {
        def function

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber)
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()
        function(new TransportInboundMessage(
                ProtocolMessages.NACK,
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
        1 * sub.onSubscribe(_)
        1 * sub.onError(_ as MuonException)
    }

    def "sends CANCEL when the Subscriber cancels the subscription"() {

        def function
        Subscription subscription

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber) {
            onSubscribe(_) >> { args ->
                subscription = args[0]
            }
        }
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        and: "The subscription is ACKed"
        function(new TransportInboundMessage(
                ProtocolMessages.ACK,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        and: "the subscription is cancelled"
        subscription.cancel()

        then:
        1 * connection.send({ TransportOutboundMessage msg ->
            msg.type == ProtocolMessages.CANCEL
        } as TransportOutboundMessage)

    }

    def "sends REQUEST when the Subscriber requests more data"() {
        def function
        Subscription subscription

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber) {
            onSubscribe(_) >> { args ->
                subscription = args[0]
            }
        }
        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        and: "The subscription is ACKed"
        function(new TransportInboundMessage(
                ProtocolMessages.ACK,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.NORMAL))

        and: "more data is requested off the subscription"
        subscription.request(100)

        then:
        1 * connection.send({ TransportOutboundMessage msg ->
            msg.type == ProtocolMessages.REQUEST &&
                    msg.metadata.request == "100"
        } as TransportOutboundMessage)
    }

    def "on DATA, calls subscriber onNext"() {
        def function
        Subscription subscription

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber)

        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
            decode(_, _, Map) >> [helloworld:"awesome"]
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        and: "data arrives from the remote"
        function(new TransportInboundMessage(
                ProtocolMessages.DATA,
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
        1 * sub.onNext([helloworld:"awesome"])

    }

    def "on COMPLETE, calls sub onComplete"() {
        def function
        Subscription subscription

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber)

        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
            decode(_, _, Map) >> [helloworld:"awesome"]
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        and: "data arrives from the remote"
        function(new TransportInboundMessage(
                ProtocolMessages.COMPLETE,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.CLOSE_CHANNEL))

        then:
        1 * sub.onComplete()
    }

    def "on ERROR, calls sub onError"() {
        def function
        Subscription subscription

        def uri = new URI("")
        def connection = Mock(ChannelConnection) {
            receive(_) >> { args ->
                function = new ChannelFunctionExecShimBecauseGroovyCantCallLambda(args[0])
            }
        }
        def sub = Mock(Subscriber)

        def codecs = Mock(Codecs) {
            encode(_, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")
            getAvailableCodecs() >> []
            decode(_, _, Map) >> [helloworld:"awesome"]
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                Map,
                codecs,
                config)

        when:
        client.start()

        and: "data arrives from the remote"
        function(new TransportInboundMessage(
                ProtocolMessages.ERROR,
                UUID.randomUUID().toString(),
                "awesome",
                "tombola",
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                [:],
                "application/json",
                [] as byte[],
                ["application/json"],
                TransportMessage.ChannelOperation.CLOSE_CHANNEL))

        then:
        1 * sub.onError(_ as MuonException)
    }
}




