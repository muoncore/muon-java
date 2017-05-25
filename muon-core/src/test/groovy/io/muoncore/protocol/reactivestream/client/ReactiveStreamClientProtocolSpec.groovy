package io.muoncore.protocol.reactivestream.client

import io.muoncore.Discovery
import io.muoncore.channel.ChannelConnection
import io.muoncore.codec.Codecs
import io.muoncore.codec.json.GsonCodec
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.exception.MuonException
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.message.MuonOutboundMessage
import io.muoncore.protocol.ChannelFunctionExecShimBecauseGroovyCantCallLambda
import io.muoncore.protocol.reactivestream.ProtocolMessages
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest
import io.muoncore.protocol.reactivestream.messages.RequestMessage
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerStack
import io.muoncore.transport.TransportEvents
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import spock.lang.Specification

class ReactiveStreamClientProtocolSpec extends Specification {

    def discovery = Mock(Discovery)

    def "sends SUBSCRIBE when the proto is started"() {

        def uri = new URI("stream://targetService/streamname?first=true&last=20")
        def connection = Mock(ChannelConnection)
        def sub = Mock(Subscriber)
        def codecs = Mock(Codecs) {
            getAvailableCodecs() >> []
        }
        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                codecs,
                config, discovery)

        when:
        client.start()

        then:
        1 * connection.send({ MuonOutboundMessage msg ->
            msg.channelOperation == MuonMessage.ChannelOperation.normal &&
                    msg.protocol == ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL &&
                    msg.step == ProtocolMessages.SUBSCRIBE &&
                    msg.targetServiceName == "targetService"
        })

        1 * codecs.encode({ ReactiveStreamSubscriptionRequest request ->
            request.args == [first:"true", last:"20"]
        } as ReactiveStreamSubscriptionRequest, _) >> new Codecs.EncodingResult([0] as byte[], "application/json")

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
                codecs,
                config, discovery)

        when:
        client.start()
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.ACK)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

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
                codecs,
                config, discovery)

        when:
        client.start()
        function(MuonMessageBuilder
                .fromService("tombola")
                .toService("awesome")
                .step(ProtocolMessages.NACK)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .contentType("application/json")
                .payload(new GsonCodec().encode([:]))
                .buildInbound())

        then:
        1 * sub.onError(_ as MuonException)
    }

    def "on Transport.ServiceNotFound received, calls subscriber.onSubscribe and subscriber error"() {
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
                codecs,
                config, discovery)

        when:
        client.start()
        function(MuonMessageBuilder
                .fromService("tombola")
                .toService("awesome")
                .step(TransportEvents.SERVICE_NOT_FOUND)
                .protocol(ProtocolMessages.PROTOCOL_FAILURE)
                .contentType("application/json")
                .payload(new GsonCodec().encode([:]))
                .buildInbound())

        then:
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
                codecs,
                config, discovery)

        when:
        client.start()

        and: "The subscription is ACKed"
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.ACK)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

        and: "the subscription is cancelled"
        subscription.cancel()

        then:
        1 * connection.send({ MuonOutboundMessage msg ->
            msg.step == ProtocolMessages.CANCEL
        } as MuonOutboundMessage)

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

        def codecs = new JsonOnlyCodecs()

        def config = new AutoConfiguration(serviceName: "awesome")

        def client = new ReactiveStreamClientProtocol(
                uri,
                connection,
                sub,
                codecs,
                config, discovery)

        when:
        client.start()

        and: "The subscription is ACKed"
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.ACK)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

        and: "more data is requested off the subscription"
        subscription.request(100)

        then:
        1 * connection.send({ MuonOutboundMessage msg ->
            RequestMessage request = codecs.decode(msg.payload, msg.contentType, RequestMessage)
            msg.step == ProtocolMessages.REQUEST &&
                    request.request == 100
        } as MuonOutboundMessage)
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
                codecs,
                config, discovery)

        when:
        client.start()

        and: "data arrives from the remote"
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.DATA)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

        then:
        1 * sub.onNext({ it.getPayload(Map) == [helloworld:"awesome"] })

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
                codecs,
                config, discovery)

        when:
        client.start()

        and: "data arrives from the remote"
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.COMPLETE)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .buildInbound())

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
                codecs,
                config, discovery)

        when:
        client.start()

        and: "data arrives from the remote"
        function(
                MuonMessageBuilder
                        .fromService("tombola")
                        .toService("awesome")
                        .step(ProtocolMessages.ERROR)
                        .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                        .contentType("application/json")
                        .payload(new GsonCodec().encode([:]))
                        .operation(MuonMessage.ChannelOperation.closed)
                        .buildInbound())

        then:
        1 * sub.onError(_ as MuonException)
    }
}
