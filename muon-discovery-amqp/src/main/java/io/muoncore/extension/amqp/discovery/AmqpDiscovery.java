package io.muoncore.extension.amqp.discovery;

import io.muoncore.Discovery;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListener;
import io.muoncore.extension.amqp.QueueListenerFactory;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AmqpDiscovery implements Discovery {

    private QueueListenerFactory queueListenerFactory;
    private AmqpConnection connection;
    private ServiceCache serviceCache;
    private Codecs codecs;

    private QueueListener listener;
    private Runnable onReady;

    private ExecutorService spinner;

    private ServiceDescriptor localDescriptor;
    private CountDownLatch countdown = new CountDownLatch(1);
    private volatile boolean started = false;

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

            startAnnouncePing();

            if (onReady != null) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onReady.run();
            }
            started = true;
            countdown.countDown();
        }
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
        if (!started) {
            try {
                countdown.await(4, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    @Override
    public void shutdown() {
        spinner.shutdownNow();
        listener.cancel();
    }
}
