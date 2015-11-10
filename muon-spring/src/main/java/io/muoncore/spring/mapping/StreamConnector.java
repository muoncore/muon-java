package io.muoncore.spring.mapping;

public class StreamConnector {

/*
    private OldMuon muon;
    private MuonStreamMethodInvocation muonStreamMethodInvocation;
    private String muonUrl;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public StreamConnector(OldMuon muon, String muonUrl, MuonStreamMethodInvocation muonStreamMethodInvocation) {
        this.muon = muon;
        this.muonUrl = muonUrl;
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

        muon.subscribe(muonUrl, muonStreamMethodInvocation.getDecodedParameterType(), localstream);
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
*/
}
