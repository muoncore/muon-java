package org.muoncore.extension.amqp;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.Discovery;
import org.muoncore.Muon;
import org.muoncore.ServiceDescriptor;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpDiscovery implements Discovery {
    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());
    private ExecutorService spinner;
    public static final String SERVICE_ANNOUNCE = "serviceAnnounce";

    private ServiceCache serviceCache;
    private AmqpBroadcast amqpBroadcast;
    private ServiceDescriptor descriptor;

    public AmqpDiscovery(String amqpUrl) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
        this(new AmqpBroadcast(
                new AmqpConnection(amqpUrl)));
    }

    public AmqpDiscovery(AmqpBroadcast amqpBroadcast) {
        this.amqpBroadcast = amqpBroadcast;
        serviceCache = new ServiceCache();
        spinner = Executors.newCachedThreadPool();
        connect();
    }

    public void connect() {
        amqpBroadcast.listenOnBroadcastEvent(SERVICE_ANNOUNCE, new Muon.EventMessageTransportListener() {
            @Override
            public void onEvent(String name, MuonMessageEvent obj) {
                Map announce = (Map) JSON.parse((String) obj.getPayload());
                serviceCache.addService(announce);
            }
        });

        startAnnouncePing();
    }

    private void startAnnouncePing() {
        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (descriptor != null) {
                            Map<String,Object> discoveryMessage = new HashMap<String, Object>();

                            discoveryMessage.put("identifier", descriptor.getIdentifier());
                            discoveryMessage.put("tags", descriptor.getTags());
                            discoveryMessage.put("connectionUrls", descriptor.getConnectionUris());

                            amqpBroadcast.broadcast(SERVICE_ANNOUNCE, MuonMessageEventBuilder.named(SERVICE_ANNOUNCE)
                                    .withContent(JSON.toString(discoveryMessage)).build());
                        }
                        Thread.sleep(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public List<ServiceDescriptor> getKnownServices() {
        List<ServiceDescriptor> services = new ArrayList<ServiceDescriptor>();

        try {
            for (Map data : serviceCache.getServices()) {
                List<URI> connectionList = null;

                connectionList = readConnectionUrls(data);

                List tagList = readTags(data);

                services.add(new ServiceDescriptor(
                        (String) data.get("identifier"),
                        tagList, connectionList));
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Unable to read a service descriptor, are you using the same protocol version?", e);
        }

        return services;
    }

    private List<URI> readConnectionUrls(Map data) throws URISyntaxException {
        Object connectionUrls = data.get("connectionUrls");
        List<URI> ret = new ArrayList<URI>();

        if (connectionUrls != null && connectionUrls instanceof List) {
            List urls = (List) connectionUrls;
            for (Object url : urls) {
                ret.add(new URI(url.toString()));
            }
        }
        return ret;
    }

    private List readTags(Map data) {
        Object tags = data.get("tags");
        List ret = new ArrayList();

        if (tags != null && tags instanceof List) {
            ret = (List) tags;
        }
        return ret;
    }

    @Override
    public ServiceDescriptor getService(URI searchUri) {
        for(ServiceDescriptor desc: getKnownServices()) {
            for (URI uri: desc.getConnectionUris()) {
                if (uri.equals(searchUri)) {
                    return desc;
                }
            }
        }
        return null;
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
