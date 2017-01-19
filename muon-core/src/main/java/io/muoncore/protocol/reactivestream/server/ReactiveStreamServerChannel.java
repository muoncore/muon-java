package io.muoncore.protocol.reactivestream.server;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.reactivestream.ProtocolMessages;
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest;
import io.muoncore.protocol.reactivestream.messages.RequestMessage;
import io.muoncore.transport.TransportEvents;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class ReactiveStreamServerChannel implements ChannelConnection<MuonInboundMessage, MuonOutboundMessage> {

    private PublisherLookup publisherLookup;
    private Subscription subscription;
    private String subscribingServiceName;
    private ChannelFunction<MuonOutboundMessage> function;
    private Codecs codecs;
    private AutoConfiguration configuration;
    private Discovery discovery;

    private List<String> acceptedContentTypes;

    public ReactiveStreamServerChannel(
            PublisherLookup publisherLookup,
            Codecs codecs,
            AutoConfiguration configuration, Discovery discovery) {
        this.publisherLookup = publisherLookup;
        this.codecs = codecs;
        this.configuration = configuration;
        this.discovery = discovery;
    }

    @Override
    public void receive(ChannelFunction<MuonOutboundMessage> function) {
        this.function = function;
    }

    @Override
    public void send(MuonInboundMessage message) {
        if (message == null) {
            handleError();
            return;
        }

        switch(message.getStep()) {
            case ProtocolMessages.SUBSCRIBE:
                handleSubscribe(message);
                break;
            case ProtocolMessages.REQUEST:
                handleRequest(message);
                break;
            case ProtocolMessages.CANCEL:
                handleCancel(message);
                break;
            case TransportEvents.CONNECTION_FAILURE:
                handleError();
                break;
            case "ChannelShutdown":
                handleError();
                break;
            default:
                sendProtocolFailureException(message);
        }
    }

    private void sendProtocolFailureException(MuonInboundMessage msg) {
        System.out.println("Don't understand " + msg.getStep());
        Map<String, String> meta = new HashMap<>();
        meta.put("SourceMessage", msg.getId());
        meta.put("SourceType", msg.getSourceServiceName());

        Codecs.EncodingResult result = codecs.encode(meta,
                discovery.getCodecsForService(msg.getSourceServiceName()));

        function.apply(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.PROTOCOL_FAILURE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(subscribingServiceName)
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .status(MuonMessage.Status.error)
                .operation(MuonMessage.ChannelOperation.closed)
                .build()
        );
    }

    private void sendNack(MuonInboundMessage msg) {
        Map<String, String> meta = new HashMap<>();

        Codecs.EncodingResult result = codecs.encode(meta,
                discovery.getCodecsForService(msg.getSourceServiceName()));

        function.apply(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.NACK)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(subscribingServiceName)
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .status(MuonMessage.Status.error)
                .build()
        );
        function.apply(MuonMessageBuilder
          .fromService(configuration.getServiceName())
          .step(ProtocolMessages.NACK)
          .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
          .toService(subscribingServiceName)
          .payload(result.getPayload())
          .contentType(result.getContentType())
          .operation(MuonMessage.ChannelOperation.closed)
          .build()
        );
    }
    private void sendAck(MuonInboundMessage msg) {
        Map<String, String> meta = new HashMap<>();

        Codecs.EncodingResult result = codecs.encode(meta,
                discovery.getCodecsForService(msg.getSourceServiceName()));

        function.apply(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.ACK)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(subscribingServiceName)
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .build()
        );

    }

    @SuppressWarnings("unchecked")
    private void handleSubscribe(MuonInboundMessage msg) {

        ReactiveStreamSubscriptionRequest subscriptionMessage  = codecs.decode(msg.getPayload(), msg.getContentType(), ReactiveStreamSubscriptionRequest.class);

        Optional<PublisherLookup.PublisherRecord> pub = publisherLookup.lookupPublisher(subscriptionMessage.getStreamName());

        if (!pub.isPresent()) {
            sendNack(msg);
        } else {
            acceptedContentTypes = Arrays.asList(discovery.getCodecsForService(msg.getSourceServiceName()));
            subscribingServiceName = msg.getSourceServiceName();

            pub.get().getPublisher().generatePublisher(subscriptionMessage).subscribe(new Subscriber() {
                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    sendAck(msg);
                }

                @Override
                public void onNext(Object o) {
                    Codecs.EncodingResult result = codecs.encode(o, acceptedContentTypes.toArray(new String[0]));

                    function.apply(MuonMessageBuilder
                            .fromService(configuration.getServiceName())
                            .step(ProtocolMessages.DATA)
                            .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                            .toService(subscribingServiceName)
                            .payload(result.getPayload())
                            .contentType(result.getContentType())
                            .build()
                    );
                }

                @Override
                public void onError(Throwable t) {

                    Map<String, String> meta = new HashMap<>();
                    meta.put("error", t.getMessage());

                    Codecs.EncodingResult result = codecs.encode(meta,
                            discovery.getCodecsForService(msg.getSourceServiceName()));

                    function.apply(MuonMessageBuilder
                            .fromService(configuration.getServiceName())
                            .step(ProtocolMessages.ERROR)
                            .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                            .toService(subscribingServiceName)
                            .payload(result.getPayload())
                            .contentType(result.getContentType())
                            .operation(MuonMessage.ChannelOperation.closed)
                            .build()
                    );

                }

                @Override
                public void onComplete() {
                    sendComplete();
                }
            });
        }
    }

    private void sendComplete() {
        Map<String, String> meta = new HashMap<>();

        Codecs.EncodingResult result = codecs.encode(meta,
                discovery.getCodecsForService(subscribingServiceName));

        function.apply(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.COMPLETE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(subscribingServiceName)
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .operation(MuonMessage.ChannelOperation.normal)
                .build()
        );
        function.apply(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.COMPLETE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(subscribingServiceName)
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .operation(MuonMessage.ChannelOperation.closed)
                .build()
        );

    }

    @Override
    public void shutdown() {
        sendComplete();
    }

    private void handleRequest(MuonInboundMessage msg) {

        if (subscription == null) {
            throw new MuonException("Unable to handle, subscription is not yet set");
        }

        RequestMessage request = codecs.decode(msg.getPayload(), msg.getContentType(), RequestMessage.class);

        subscription.request(request.getRequest());
    }

    private void handleCancel(MuonInboundMessage msg) {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    private void handleError() {
        if (subscription != null) {
            subscription.cancel();
        }
    }
}
