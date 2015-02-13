package org.muoncore.extension.zeromq;

import org.muoncore.Muon;
import org.muoncore.MuonClient;
import org.muoncore.MuonService;
import org.muoncore.MuonStreamGenerator;
import org.muoncore.extension.amqp.AmqpBroadcast;
import org.muoncore.extension.amqp.AmqpConnection;
import org.muoncore.extension.amqp.AmqpQueues;
import org.muoncore.extension.amqp.AmqpResources;
import org.muoncore.extension.amqp.stream.AmqpStream;
import org.muoncore.transports.*;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ZeroMqEventTransport
        implements
        MuonStreamTransport {

    private Logger log = Logger.getLogger(ZeroMqEventTransport.class.getName());

    private String serviceName;

    public ZeroMqEventTransport(String serviceName, List<String> tags){
        this.serviceName = serviceName;

    }

    @Override
    public void provideStreamSource(String streamName, MuonStreamGenerator sourceOfData) {

    }

    @Override
    public void subscribeToStream(String url, Map<String, String> params, Subscriber subscriber) throws URISyntaxException {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public String getUrlScheme() {
        return "zeromq";
    }

    @Override
    public URI getLocalConnectionURI() throws URISyntaxException {
        return new URI("zeromq://localhost:2213");
    }
}
