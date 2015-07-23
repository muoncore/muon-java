package io.muoncore.spring.mapping;

import io.muoncore.Muon;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.fn.Consumer;
import reactor.rx.broadcast.Broadcaster;

import java.net.URISyntaxException;
import java.util.Map;

public class StreamConnector {

    private Map<String, String> params;
    private Muon muon;
    private MuonStreamMethodInvocation muonStreamMethodInvocation;
    private String muonUrl;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public StreamConnector(Muon muon, String muonUrl, Map<String, String> params, MuonStreamMethodInvocation muonStreamMethodInvocation) {
        this.muon = muon;
        this.muonUrl = muonUrl;
        this.params = params;
        this.muonStreamMethodInvocation = muonStreamMethodInvocation;
    }

    public synchronized void safeConnectToStream() throws URISyntaxException {
        if (connectionState == ConnectionState.DISCONNECTED) {
            connectToStream();
        }
    }

    private void connectToStream() throws URISyntaxException {
        connectionState = ConnectionState.CONNECTING;
        Broadcaster<Object> localstream = Broadcaster.create();
        localstream.subscribe(new Subscriber<Object>() {
            @Override
            public void onSubscribe(Subscription s) {}

            @Override
            public void onNext(Object o) {}

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                connectionState = ConnectionState.DISCONNECTED;
            }

            @Override
            public void onComplete() {}
        });

        localstream.consume(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                muonStreamMethodInvocation.invoke(o);
            }
        });

        muon.subscribe(muonUrl, muonStreamMethodInvocation.getDecodedParameterType(), params, localstream);
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
