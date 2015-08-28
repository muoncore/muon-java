package io.muoncore.extension.amqp;

import io.muoncore.Muon;
import io.muoncore.MuonService;
import io.muoncore.codec.Codecs;
import io.muoncore.log.EventLogger;
import io.muoncore.transport.MuonMessageEvent;
import io.muoncore.transport.MuonMessageEventBuilder;
import io.muoncore.transport.resource.MuonResourceEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AmqpResources {

    static String EXCHANGE_NAME ="muon-resource";

    private Logger log = Logger.getLogger(AMQPEventTransport.class.getName());

    private AmqpQueues queues;

    private ExecutorService spinner;

    private Map<String, Muon.EventResourceTransportListener> resourceListeners = new HashMap<String, Muon.EventResourceTransportListener>();

    public AmqpResources(final AmqpQueues queues,
                         String serviceName, final Codecs codecs) throws IOException {
        this.queues = queues;

        spinner = Executors.newCachedThreadPool();

        String resourceQueue = "resource-listen." + serviceName;

        log.info("Opening resource queue to listen for resource requests " + resourceQueue);

        queues.listenOnQueueEvent(resourceQueue, Void.class, new Muon.EventMessageTransportListener() {
            @Override
            public void onEvent(String name, MuonMessageEvent request) {

                String verb = (String) request.getHeaders().get("verb");
                String resource =  (String) request.getHeaders().get("RESOURCE");
                String key = resource + "-" + verb;
                String responseQueue = (String) request.getHeaders().get("RESPONSE_QUEUE");

                //find the listener
                Muon.EventResourceTransportListener listener = resourceListeners.get(key);

                if (listener == null) {
                    log.fine("Couldn't find a matching listener for " + key);
                    queues.send(responseQueue, MuonMessageEventBuilder.named("")
                            .withHeader("Status", "404")
                            .withHeader("RequestID", (String) request.getHeaders().get("RequestID"))
                            .withContent("").build());
                    return;
                }

                MuonResourceEvent ev = new MuonResourceEvent(null);
                ev.setBinaryEncodedContent(request.getBinaryEncodedContent());

                ev.getHeaders().putAll(request.getHeaders());
                ev.setContentType((String) request.getHeaders().get("Content-Type"));
                Object response = new HashMap();
                //TODO, frp this crap, esp the future.
                try {
                    response = listener.onEvent(resource, ev).get();
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Resource handler failed to process correctly", ex);
                }
                log.finer("Sending" + response);

                byte[] responseBytes = new byte[0];
                if (response != null) {
                    responseBytes = codecs.encodeToByte(response);
                }

                MuonMessageEvent responseEvent = new MuonMessageEvent("", responseBytes);
                responseEvent.addHeader("Status", "200");
                responseEvent.addHeader("RequestID", (String) request.getHeaders().get("RequestID"));

                //TODO, detect the content type from the codec!
                responseEvent.setContentType("application/json");

                EventLogger.logEvent(responseQueue, responseEvent);
                    
                queues.send(responseQueue, responseEvent);
            }
        });
    }

    public MuonService.MuonResult emitForReturn(String eventName, MuonResourceEvent event) {

        final MuonService.MuonResult ret = new MuonService.MuonResult();
        ret.setEvent(new MuonResourceEvent(null));
        ret.getResponseEvent().getHeaders().put("Status", "404");
        final CountDownLatch responseReceivedSignal = new CountDownLatch(1);

        try {
            String returnQueue = "resourcereturn." + UUID.randomUUID().toString();
            String resourceQueue = "resource-listen." + event.getUri().getHost();

            Muon.EventMessageTransportListener listener = new Muon.EventMessageTransportListener() {
                @Override
                public void onEvent(String name, MuonMessageEvent obj) {
                    MuonResourceEvent resEv = new MuonResourceEvent(null);
                    resEv.getHeaders().put("Status", "200");
                    resEv.getHeaders().putAll(obj.getHeaders());
                    resEv.setBinaryEncodedContent(obj.getBinaryEncodedContent());

                    ret.setEvent(resEv);
                    ret.setSuccess(true);

                    responseReceivedSignal.countDown();
                }
            };

            queues.listenOnQueueEvent(returnQueue, Void.class, listener);

            String uuid = UUID.randomUUID().toString();

            MuonMessageEvent messageEvent = new MuonMessageEvent(event.getResource(), event.getDecodedContent());
            messageEvent.getHeaders().putAll(event.getHeaders());
            messageEvent.getHeaders().put("RESOURCE", event.getResource());
            messageEvent.getHeaders().put("RESPONSE_QUEUE",returnQueue);
            messageEvent.getHeaders().put("RequestID", uuid);

            messageEvent.setEncodedBinaryContent(event.getBinaryEncodedContent());
            queues.send(resourceQueue, messageEvent);

            responseReceivedSignal.await(15, TimeUnit.SECONDS);

            queues.removeListener(listener);

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
