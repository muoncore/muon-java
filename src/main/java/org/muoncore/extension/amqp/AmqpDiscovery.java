package org.muoncore.extension.amqp;

import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.Muon;
import org.muoncore.ServiceDescriptor;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.muoncore.transports.MuonMessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class AmqpDiscovery {
    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());
    private ExecutorService spinner;
    public static final String SERVICE_ANNOUNCE = "serviceAnnounce";

    private ServiceCache serviceCache;
    private AmqpBroadcast amqpBroadcast;
    private String serviceName;
    private AMQPEventTransport parent;
    private List<String> tags;

    public AmqpDiscovery(String serviceName,
                         List<String> tags,
                          AmqpBroadcast amqpBroadcast,
                          AMQPEventTransport parent) {
        this.parent = parent;
        this.tags = tags;
        this.serviceName = serviceName;
        this.amqpBroadcast = amqpBroadcast;
        serviceCache = new ServiceCache();
        spinner = Executors.newCachedThreadPool();
        start();
    }

    private void start() {
        amqpBroadcast.listenOnBroadcastEvent(SERVICE_ANNOUNCE, new Muon.EventMessageTransportListener() {
            @Override
            public void onEvent(String name, MuonMessageEvent obj) {
                log.fine("Service announced " + obj.getPayload());
                Map announce = (Map) JSON.parse((String) obj.getPayload());
                serviceCache.addService(announce);
            }
        });

        startAnnouncePing();
    }

    public void startAnnouncePing() {

        Map<String,Object> discovery = new HashMap<String, Object>();
        discovery.put("identifier", serviceName);
        discovery.put("tags", tags);

        final String discoveryMessage = JSON.toString(discovery);

        spinner.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        amqpBroadcast.broadcast(SERVICE_ANNOUNCE, MuonMessageEventBuilder.named(SERVICE_ANNOUNCE)
                                .withContent(discoveryMessage).build());
                        Thread.sleep(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public List<ServiceDescriptor> discoverServices() {
        List<ServiceDescriptor> services = new ArrayList<ServiceDescriptor>();


        for(Map data: serviceCache.getServices()) {
            List tagList = new ArrayList();
            Object tags = data.get("tags");

            if (tags != null && tags instanceof List) {
                tagList = (List) tags;
            }

            services.add(new ServiceDescriptor(
                    (String) data.get("identifier"),
                    tagList,
                    parent));
        }

        return services;
    }

    public void shutdown() {
        spinner.shutdown();
    }
}
