package io.muoncore.extension.amqp.discovery;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.Codecs;
import io.muoncore.extension.amqp.AmqpConnection;
import io.muoncore.extension.amqp.QueueListener;
import io.muoncore.extension.amqp.QueueListenerFactory;
import io.muoncore.extension.amqp.QueueMessageBuilder;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AmqpDiscovery implements Discovery {

  private QueueListenerFactory queueListenerFactory;
  private AmqpConnection connection;
  private ServiceCache serviceCache;
  final private Codecs codecs;

  private QueueListener listener;
  private DiscoveryOnReady onReady;

  private ExecutorService spinner;

  private InstanceDescriptor localDescriptor;
  private CountDownLatch countdown = new CountDownLatch(1);
  private volatile boolean started = false;
  private volatile boolean executedOnReady = false;

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
        serviceCache.addService(codecs.decode(data.getBody(), data.getContentType(), InstanceDescriptor.class));
      });

      startAnnouncePing();

      spinner.execute(() -> {
        try {
          Thread.sleep(4000);
          executedOnReady = true;
          if (onReady != null) {
            onReady.call();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      started = true;
      countdown.countDown();
    }
  }

  private void startAnnouncePing() {
    spinner.execute(() -> {
      while (true) {
        try {
          if (localDescriptor != null) {
            Codecs.EncodingResult payload = codecs.encode(localDescriptor, new String[]{"application/json"});

            if (payload != null && !payload.isFailed()) {
              try {
                connection.broadcast(
                  QueueMessageBuilder.queue("discovery")
                    .body(payload.getPayload())
                    .contentType(payload.getContentType()).build()
                );
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          Thread.sleep(3000);
        } catch (InterruptedException e) { }
      }
    });
  }

  @Override
  public List<String> getServiceNames() {
    if (!started) {
      try {
        countdown.await(4, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return serviceCache.getServices().stream().map(ServiceDescriptor::getIdentifier).collect(Collectors.toList());
  }

  @Override
  public Optional<ServiceDescriptor> getServiceNamed(String name) {
    return serviceCache.getServices().stream().filter(serviceDescriptor -> serviceDescriptor.getIdentifier().equals(name)).findFirst();
  }

  @Override
  public Optional<ServiceDescriptor> getServiceWithTags(String... tags) {
    return serviceCache.getServices().stream().filter(serviceDescriptor ->
      serviceDescriptor.getTags().containsAll(Arrays.asList(tags))).findFirst();
  }

  @Override
  public void advertiseLocalService(InstanceDescriptor descriptor) {
    this.localDescriptor = descriptor;
  }

  @Override
  public void onReady(DiscoveryOnReady onReady) {
    synchronized (this) {
      if (executedOnReady) {
        try {
          onReady.call();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        this.onReady = onReady;
      }
    }
  }

  @Override
  public void shutdown() {
    spinner.shutdownNow();
    listener.cancel();
    connection.close();
  }
}
