package io.muoncore.extension.amqp.discovery;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListener;
import io.muoncore.extension.amqp.QueueListenerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmqpDiscovery implements Discovery {

    private QueueListenerFactory queueListenerFactory;
    private AmqpConnection connection;
    private ServiceCache serviceCache;
    private Codecs codecs;

    private QueueListener listener;
    private Runnable onReady;

    private ExecutorService spinner;

    private ServiceDescriptor localDescriptor;

    public AmqpDiscovery(
            QueueListenerFactory queueListenerFactory,
            AmqpConnection connection,
            ServiceCache cache,
            Codecs codecs) {
        this.queueListenerFactory = queueListenerFactory;
        this.connection = connection;
        this.serviceCache = cache;
        this.codecs = codecs;
        this.spinner = Executors.newCachedThreadPool();
    }

    public void start() {
        synchronized (this) {
            listener = queueListenerFactory.listenOnBroadcast("discovery", data -> {
                serviceCache.addService(codecs.decode(data.getBody(), data.getContentType(), ServiceDescriptor.class));
            });
            if (onReady != null) {
                onReady.run();
            }
        }
        startAnnouncePing();
    }

    private void startAnnouncePing() {
        spinner.execute(() -> {
            try {
                while (true) {
                    if (localDescriptor != null) {
                        Codecs.EncodingResult payload = codecs.encode(localDescriptor, new String[] {"application/json" });

                        if (!payload.isFailed()) {
                            try {
                                connection.broadcast(new QueueListener.QueueMessage(
                                        "broadcast", "discovery", payload.getPayload(), new HashMap<>(), payload.getContentType()
                                ));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<ServiceDescriptor> getKnownServices() {
        return serviceCache.getServices();
    }

    @Override
    public void advertiseLocalService(ServiceDescriptor descriptor) {
        this.localDescriptor = descriptor;
    }

    @Override
    public void onReady(Runnable onReady) {
        synchronized (this) {
            if (listener != null) {
                onReady.run();
            } else {
                this.onReady = onReady;
            }
        }
    }
}
