package io.muoncore.protocol.reactivestream.client;

import io.muoncore.Discovery;
import io.muoncore.channel.ChannelConnection;
import io.muoncore.codec.Codecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.exception.MuonException;
import io.muoncore.protocol.reactivestream.ProtocolMessages;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerStack;
import io.muoncore.transport.TransportInboundMessage;
import io.muoncore.transport.TransportOutboundMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReactiveStreamClientProtocol<T> {

    private ChannelConnection<TransportOutboundMessage, TransportInboundMessage> transportConnection;
    private Subscriber<T> subscriber;
    private Class<T> type;
    private URI uri;
    private AutoConfiguration configuration;
    private Codecs codecs;
    private Discovery discovery;

    public ReactiveStreamClientProtocol(URI uri,
                                        ChannelConnection<TransportOutboundMessage, TransportInboundMessage> transportConnection,
                                        Subscriber<T> subscriber,
                                        Class<T> type,
                                        Codecs codecs,
                                        AutoConfiguration configuration) {
        this.uri = uri;
        this.transportConnection = transportConnection;
        this.subscriber = subscriber;
        this.type = type;
        this.codecs = codecs;
        this.configuration = configuration;
    }

    public void start() {
        transportConnection.receive( msg -> handleMessage(msg));
        sendSubscribe();
    }

    private void handleMessage(TransportInboundMessage msg) {
        switch(msg.getType()) {
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
//                subscriber.onError(new MuonException("Stream does not exist"));
                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
//                        sendRequest(n);
                        subscriber.onError(new MuonException("Stream does not exist"));
                    }

                    @Override
                    public void cancel() {
//                        sendCancel();
                        subscriber.onError(new MuonException("Stream does not exist"));
                    }
                });
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
        Codecs.EncodingResult result = codecs.encode(new Object(), new String[]{"application/json"});

        Map<String, String> meta = new HashMap<>();
        meta.put("request", String.valueOf(n));

        transportConnection.send(new TransportOutboundMessage(
                ProtocolMessages.REQUEST,
                UUID.randomUUID().toString(),
                uri.getHost(),
                configuration.getServiceName(),
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                meta,
                result.getContentType(),
                result.getPayload(),
                Arrays.asList(codecs.getAvailableCodecs())
        ));
    }

    private void sendCancel() {
        Codecs.EncodingResult result = codecs.encode(new Object(), new String[]{"application/json"});

        transportConnection.send(new TransportOutboundMessage(
                ProtocolMessages.CANCEL,
                UUID.randomUUID().toString(),
                uri.getHost(),
                configuration.getServiceName(),
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                new HashMap<>(),
                result.getContentType(),
                result.getPayload(),
                Arrays.asList(codecs.getAvailableCodecs())
        ));
    }

    private void sendSubscribe() {
        Codecs.EncodingResult result = codecs.encode(new Object(), new String[]{"application/json"});

        Map<String, String> meta = new HashMap<>();
        meta.put("streamName", uri.getPath());

        transportConnection.send(new TransportOutboundMessage(
                ProtocolMessages.SUBSCRIBE,
                UUID.randomUUID().toString(),
                uri.getHost(),
                configuration.getServiceName(),
                ReactiveStreamServerStack.REACTIVE_STREAM_PROTOCOL,
                meta,
                result.getContentType(),
                result.getPayload(),
                Arrays.asList(codecs.getAvailableCodecs())
        ));
    }
}
