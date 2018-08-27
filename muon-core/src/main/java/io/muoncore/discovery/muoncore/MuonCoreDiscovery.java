package io.muoncore.discovery.muoncore;

import io.muoncore.Discovery;
import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.codec.types.MuonCodecTypes;
import io.muoncore.transport.ServiceCache;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class MuonCoreDiscovery implements Discovery {

  private MuonCoreConnection connection;
  private ServiceCache cache = new ServiceCache(true);
  private final List<String> services = new ArrayList<>();
  private final Codecs codecs = new JsonOnlyCodecs();
  private final CountDownLatch latch = new CountDownLatch(1);
  private boolean connected = false;

  private final Map<String, Consumer<MuonCoreMessage>> requests = new HashMap<>();

  private InstanceDescriptor descriptor;

  MuonCoreDiscovery(MuonCoreConnection connection) {
    this.connection = connection;
    connection.setDiscovery(this);
    connection.start();
  }

  @Override
  public List<String> getServiceNames() {
    log.debug("Returning services " + services);
    return new ArrayList<>(services);
  }

  @Override
  public Optional<ServiceDescriptor> getServiceNamed(String name) {

    if (!services.contains(name)) {
      return Optional.empty();
    }

    if (cache.getService(name).isPresent()) {
      return cache.getService(name);
    }

    try {
      connection.send(new MuonCoreMessage(
        "discovery", "refreshservice", "", name.getBytes()
      ));
    } catch (IOException e) {
      log.info("Error sending discovery refresh", e);
    }

    for (int i=0; i < 10; i++) {
      Optional<ServiceDescriptor> descriptor = cache.getService(name);
      if (descriptor != null) {
        return descriptor;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {}
    }

    log.debug("Waited for service, not found, abandoning ... ");
    return Optional.empty();
  }

  @Override
  public Optional<ServiceDescriptor> getServiceWithTags(String... tags) {

    Optional<ServiceDescriptor> localDescriptor = cache.getServices().stream().filter(
      serviceDescriptor -> serviceDescriptor.getTags().containsAll(Arrays.asList(tags))).findFirst();

    if (localDescriptor.isPresent()) {
      return localDescriptor;
    }

    String id = UUID.randomUUID().toString();
    CountDownLatch latch = new CountDownLatch(1);

    HashSet<ServiceDescriptor> descriptor = new HashSet<>();

    requests.put(id, message -> {
      try {
        InstanceDescriptor desc = codecs.decode(message.getData(), "application/json", InstanceDescriptor.class);
        cache.addService(desc);
        descriptor.add(cache.getService(desc.getIdentifier()).get());
      } finally {
        latch.countDown();
        requests.remove(id);
      }
    });

    try {
      log.info("REFRESH WITH TAGS");
      connection.send(new MuonCoreMessage(
        "discovery", "refreshservicetags", id, codecs.encode(tags, codecs.getAvailableCodecs()).getPayload()
      ));
    } catch (IOException e) {
      log.info("Error sending discovery refresh", e);
    }

    try {
      latch.await(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {

    }

    if (descriptor.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(descriptor.iterator().next());
  }

  @Override
  public void advertiseLocalService(InstanceDescriptor descriptor) {
    this.descriptor = descriptor;
    advertiseCurrentService();
  }

  public void advertiseCurrentService() {
    if (this.descriptor == null) {
      return;
    }
    try {
      byte[] dats = codecs.encode(descriptor, codecs.getAvailableCodecs()).getPayload();

      connection.send(new MuonCoreMessage(
        "discovery", "advertise","", dats
      ));
    } catch (IOException e) {
      log.warn("Unable to advertise service, will force reconnect");
      connection.reconnect();
    }
  }

  @Override
  public void onReady(DiscoveryOnReady onReady) {
    if (!connected) {
      try {
        latch.await(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    onReady.call();
  }

  @Override
  public void shutdown() {
    connection.shutdown();
  }

  public void handle(MuonCoreMessage message) {

    if (message.getStep().equals("services")) {
      List<String> svc = codecs.decode(message.getData(), "application/json", MuonCodecTypes.listOf(String.class));
      services.clear();
      services.addAll(svc);
      connected = true;
      latch.countDown();
    } else if (message.getStep().equals("refreshservice")) {
      InstanceDescriptor desc = codecs.decode(message.getData(), "application/json", InstanceDescriptor.class);
      cache.addService(desc);
    } else if (message.getStep().equals("refreshservicetags")) {
      requests.get(message.getCorrelationId()).accept(message);
    } else {
      log.warn("Discovery: Unknown protocol step " + message.getStep());
    }
  }
}
