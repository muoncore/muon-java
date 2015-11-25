package io.muoncore.protocol.reactivestream.server;

import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.reactivestream.ProtocolMessages;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportMessage;
import io.muoncore.transport.TransportOutboundMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class ReactiveStreamServerChannel implements ChannelConnection<TransportInboundMessage, TransportOutboundMessage> {

    private PublisherLookup publisherLookup;
    private Subscription subscription;
    private String subscribingServiceName;
    private ChannelFunction<TransportOutboundMessage> function;
    private Codecs codecs;
    private AutoConfiguration configuration;

    private List<String> acceptedContentTypes;

    public ReactiveStreamServerChannel(
            PublisherLookup publisherLookup,
            Codecs codecs,
            AutoConfiguration configuration) {
        this.publisherLookup = publisherLookup;
        this.codecs = codecs;
        this.configuration = configuration;
    }

    @Override
    public void receive(ChannelFunction<TransportOutboundMessage> function) {
        this.function = function;
    }

    @Override
    public void send(TransportInboundMessage message) {
        switch(message.getType()) {
            case ProtocolMessages.SUBSCRIBE:
                handleSubscribe(message);
                break;
            case ProtocolMessages.REQUEST:
                handleRequest(message);
                break;
            case ProtocolMessages.CANCEL:
                handleCancel(message);
                break;
        }
    }
//
//    The SUBSCRIBE message doesn't appear to have made it this far.
//
//    check if a channel is being correctly established. Then check if the data is correctly propogating along the channel
//            to the serverstack
//




    private void sendNack() {
        Map<String, String> meta = new HashMap<>();

        function.apply(new TransportOutboundMessage(
                ProtocolMessages.NACK,
                UUID.randomUUID().toString(),
                subscribingServiceName,
                configuration.getServiceName(),
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                meta,
                "application/json",
                new byte[0],
                Arrays.asList(codecs.getAvailableCodecs()),
                TransportMessage.ChannelOperation.NORMAL));
    }
    private void sendAck() {
        Map<String, String> meta = new HashMap<>();

        function.apply(new TransportOutboundMessage(
                ProtocolMessages.ACK,
                UUID.randomUUID().toString(),
                subscribingServiceName,
                configuration.getServiceName(),
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                meta,
                "application/json",
                new byte[0],
                Arrays.asList(codecs.getAvailableCodecs()),
                TransportMessage.ChannelOperation.NORMAL));
    }

    @SuppressWarnings("unchecked")
    private void handleSubscribe(TransportInboundMessage msg) {

        Optional<PublisherLookup.PublisherRecord> pub = publisherLookup.lookupPublisher(msg.getMetadata().get("streamName"));

        if (!pub.isPresent()) {
            sendNack();
        } else {
            acceptedContentTypes = msg.getSourceAvailableContentTypes();
            subscribingServiceName = msg.getSourceServiceName();
            pub.get().getPublisher().subscribe(new Subscriber() {
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    sendAck();
                }

                @Override
                public void onNext(Object o) {
                    Codecs.EncodingResult result = codecs.encode(o, acceptedContentTypes.toArray(new String[0]));

                    function.apply(
                            new TransportOutboundMessage(
                                    ProtocolMessages.DATA,
                                    UUID.randomUUID().toString(),
                                    subscribingServiceName,
                                    configuration.getServiceName(),
                                    ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                                    Collections.emptyMap(),
                                    result.getContentType(),
                                    result.getPayload(),
                                    Arrays.asList(codecs.getAvailableCodecs()),
                                    TransportMessage.ChannelOperation.NORMAL));
                }

                @Override
                public void onError(Throwable t) {
                    function.apply(
                            new TransportOutboundMessage(
                                    ProtocolMessages.ERROR,
                                    UUID.randomUUID().toString(),
                                    subscribingServiceName,
                                    configuration.getServiceName(),
                                    ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                                    Collections.emptyMap(),
                                    "text/plain",
                                    new byte[0],
                                    Arrays.asList(codecs.getAvailableCodecs()),
                                    TransportMessage.ChannelOperation.CLOSE_CHANNEL));
                }

                @Override
                public void onComplete() {
                    function.apply(
                            new TransportOutboundMessage(
                                    ProtocolMessages.COMPLETE,
                                    UUID.randomUUID().toString(),
                                    subscribingServiceName,
                                    configuration.getServiceName(),
                                    ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                                    Collections.emptyMap(),
                                    "text/plain",
                                    new byte[0],
                                    Arrays.asList(codecs.getAvailableCodecs()),
                                    TransportMessage.ChannelOperation.CLOSE_CHANNEL));
                }
            });
        }
    }

    private void handleRequest(TransportInboundMessage msg) {
        if (subscription == null) {
            throw new MuonException("Unable to handle, subscription is not yet set");
        }
        subscription.request(Long.parseLong(msg.getMetadata().get("request")));
    }

    private void handleCancel(TransportInboundMessage msg) {
        subscription.cancel();
    }
}
