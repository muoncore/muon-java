package io.muoncore.protocol.reactivestream.client;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonEncodingException;
import io.muoncore.exception.MuonException;
import io.muoncore.message.MuonInboundMessage;
import io.muoncore.message.MuonMessageBuilder;
import io.muoncore.message.MuonOutboundMessage;
import io.muoncore.protocol.reactivestream.ProtocolMessages;
import io.muoncore.protocol.reactivestream.messages.ReactiveStreamSubscriptionRequest;
import io.muoncore.protocol.reactivestream.messages.RequestMessage;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerStack;
import io.muoncore.transport.TransportEvents;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReactiveStreamClientProtocol<T> {

    private ChannelConnection<MuonOutboundMessage, MuonInboundMessage> transportConnection;
    private Subscriber<T> subscriber;
    private Type type;
    private URI uri;
    private AutoConfiguration configuration;
    private Codecs codecs;
    private Discovery discovery;

    public ReactiveStreamClientProtocol(URI uri,
                                        ChannelConnection<MuonOutboundMessage, MuonInboundMessage> transportConnection,
                                        Subscriber<T> subscriber,
                                        Type type,
                                        Codecs codecs,
                                        AutoConfiguration configuration,
                                        Discovery discovery) {
        this.uri = uri;
        this.transportConnection = transportConnection;
        this.subscriber = subscriber;
        this.type = type;
        this.codecs = codecs;
        this.configuration = configuration;
        this.discovery = discovery;
    }

    public void start()  {
        transportConnection.receive( msg -> handleMessage(msg));

        ReactiveStreamSubscriptionRequest request = new ReactiveStreamSubscriptionRequest(uri.getPath());

        splitQuery(uri).forEach(request::arg);

        sendSubscribe(request);
    }

    public static Map<String, String> splitQuery(URI url) {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = url.getQuery();
        if (query == null || query.trim().length() == 0) return query_pairs;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new MuonEncodingException("Unable to decode data " + e.getMessage(), e);
            }
        }
        return query_pairs;
    }

    private void handleMessage(MuonInboundMessage msg) {
        switch(msg.getStep()) {
            case ProtocolMessages.ACK:
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        sendRequest(n);
                    }

                    @Override
                    public void cancel() {
                        sendCancel();
                    }
                });
                break;
            case ProtocolMessages.NACK:
                subscriber.onError(new MuonException("Stream does not exist"));
                break;
            case TransportEvents.SERVICE_NOT_FOUND:
                subscriber.onError(new MuonException("Service " + msg.getSourceServiceName() + " does not exist"));
                break;
            case ProtocolMessages.DATA:
                subscriber.onNext(codecs.decode(msg.getPayload(), msg.getContentType(), type));
                break;
            case ProtocolMessages.ERROR:
                subscriber.onError(new MuonException());
                break;
            case ProtocolMessages.COMPLETE:
                subscriber.onComplete();
        }
    }

    private void sendRequest(long n) {

        RequestMessage requestMessage = new RequestMessage(n);

        Codecs.EncodingResult result = codecs.encode(requestMessage, discovery.getCodecsForService(uri.getHost()));

        transportConnection.send(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.REQUEST)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(uri.getHost())
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .build()
        );
    }

    private void sendCancel() {
        Codecs.EncodingResult result = codecs.encode(new Object(), discovery.getCodecsForService(uri.getHost()));

        transportConnection.send(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.CANCEL)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(uri.getHost())
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .build()
        );

    }

    private void sendSubscribe(ReactiveStreamSubscriptionRequest subscriptionRequest) {

        Codecs.EncodingResult result = codecs.encode(subscriptionRequest, discovery.getCodecsForService(uri.getHost()));

        transportConnection.send(MuonMessageBuilder
                .fromService(configuration.getServiceName())
                .step(ProtocolMessages.SUBSCRIBE)
                .protocol(ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL)
                .toService(uri.getHost())
                .payload(result.getPayload())
                .contentType(result.getContentType())
                .build()
        );
    }
}
