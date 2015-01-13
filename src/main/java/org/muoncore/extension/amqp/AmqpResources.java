package org.muoncore.extension.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import org.eclipse.jetty.util.ajax.JSON;
import org.muoncore.Muon;
import org.muoncore.MuonService;
import org.muoncore.transports.MuonMessageEvent;
import org.muoncore.transports.MuonMessageEventBuilder;
import org.muoncore.transports.MuonResourceEvent;
import org.muoncore.transports.MuonResourceEventBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AmqpResources {

    static String EXCHANGE_NAME ="muon-resource";

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private AmqpQueues queues;

    private ExecutorService spinner;
    private String serviceName;

    private Map<String, Muon.EventResourceTransportListener> resourceListeners = new HashMap<String, Muon.EventResourceTransportListener>();

    public AmqpResources(final AmqpQueues queues, String serviceName) throws IOException {
        this.queues = queues;
        this.serviceName = serviceName;

        spinner = Executors.newCachedThreadPool();

        String resourceQueue = "resource-listen." + serviceName;

        log.info("Opening resource queue to listen for resource requests " + resourceQueue);

        queues.listenOnQueueEvent(resourceQueue, new Muon.EventMessageTransportListener() {
            @Override
            public void onEvent(String name, MuonMessageEvent request) {

                String verb = request.getHeaders().get("verb");
                String resource =  request.getHeaders().get("RESOURCE");
                String key = resource + "-" + verb;
                String responseQueue = request.getHeaders().get("RESPONSE_QUEUE");

                //find the listener
                Muon.EventResourceTransportListener listener = resourceListeners.get(key);

                if (listener == null) {
                    log.fine("Couldn't find a matching listener for " + key);
                    queues.send(responseQueue, MuonMessageEventBuilder.named("")
                            .withHeader("Status", "404")
                            .withContent("{}").build());

                    return;
                }

                MuonResourceEvent ev = MuonResourceEventBuilder.textMessage(request.getPayload().toString())
                        .withMimeType(request.getMimeType())
                        .build();

                ev.getHeaders().putAll(request.getHeaders());

                String response = listener.onEvent(resource, ev).toString();
                log.finer("Sending" + response);

                queues.send(responseQueue, MuonMessageEventBuilder.named("")
                        .withHeader("Status", "200")
                        .withContent(response).build());
            }
        });
    }

    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {

        final MuonService.MuonResult ret = new MuonService.MuonResult();
        final CountDownLatch responseReceivedSignal = new CountDownLatch(1);

        try {
            String returnQueue = "resourcereturn." + UUID.randomUUID().toString();
            String resourceQueue = "resource-listen." + event.getUri().getHost();

            queues.listenOnQueueEvent(returnQueue, new Muon.EventMessageTransportListener() {
                @Override
                public void onEvent(String name, MuonMessageEvent obj) {

                    MuonResourceEventBuilder builder = MuonResourceEventBuilder.textMessage(obj.getPayload().toString())
                            .withMimeType("application/json");

                    MuonResourceEvent resEv = new MuonResourceEvent(obj.getPayload());
                    resEv.getHeaders().putAll(obj.getHeaders());

                    ret.setEvent(builder.build());
                    responseReceivedSignal.countDown();
                }
            });

            MuonMessageEvent messageEvent = new MuonMessageEvent(event.getResource(), "application/json", event.getPayload());
            messageEvent.getHeaders().putAll(event.getHeaders());
            messageEvent.getHeaders().put("RESOURCE", event.getResource());
            messageEvent.getHeaders().put("RESPONSE_QUEUE",returnQueue);

            queues.send(resourceQueue, messageEvent);

            responseReceivedSignal.await(2, TimeUnit.SECONDS);

            return ret;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void listenOnResource(final String resource, final String verb, final Muon.EventResourceTransportListener listener) {
        String key = resource + "-" + verb;
        log.info("Register listener for " + key);
        resourceListeners.put(key, listener);
    }

    public void shutdown() {
        spinner.shutdown();
    }

}
