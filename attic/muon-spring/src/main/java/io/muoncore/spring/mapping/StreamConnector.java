package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.rx.broadcast.Broadcaster;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class StreamConnector {

    private Muon muon;
    private MuonStreamMethodInvocation muonStreamMethodInvocation;
    private String muonUrl;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public StreamConnector(Muon muon, String muonUrl, MuonStreamMethodInvocation muonStreamMethodInvocation) {
        this.muon = muon;
        this.muonUrl = muonUrl;
        this.muonStreamMethodInvocation = muonStreamMethodInvocation;
    }

    public synchronized void safeConnectToStream() throws URISyntaxException, UnsupportedEncodingException {
        if (connectionState == ConnectionState.DISCONNECTED) {
            connectToStream();
        }
    }

    private void connectToStream() throws URISyntaxException, UnsupportedEncodingException {
        connectionState = ConnectionState.CONNECTING;
        Broadcaster<Object> localstream = Broadcaster.create();
        localstream.subscribe(new Subscriber<Object>() {
            @Override
            public void onSubscribe(Subscription s) {
            }

            @Override
            public void onNext(Object o) {
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                connectionState = ConnectionState.DISCONNECTED;
            }

            @Override
            public void onComplete() {
            }
        });

        localstream.consume(muonStreamMethodInvocation::invoke);

        muon.subscribe(new URI(muonUrl), muonStreamMethodInvocation.getDecodedParameterType(), localstream);
        if (connectionState == ConnectionState.CONNECTING) {
            connectionState = ConnectionState.CONNECTED;
        }
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public String getMuonUrl() {
        return muonUrl;
    }

    private enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED
    }
}
