package io.muoncore.transport;

import io.muoncore.InstanceDescriptor;
import io.muoncore.ServiceDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ServiceCache {

  static final int EXPIRY = 5000;

  private Map<String, Entry> serviceCache = Collections.synchronizedMap(new HashMap<>());

  private boolean expiring = true;

  public ServiceCache() {
    this.expiring = true;
  }

  public ServiceCache(boolean expiring) {
    this.expiring = expiring;
  }

  public void addService(InstanceDescriptor service) {
    service.validate();
    Entry entry = serviceCache.get(service.getIdentifier());
    if (entry == null) {
      entry = new Entry(service);
    } else {
      entry.touch(service);
    }
    serviceCache.put(
      service.getIdentifier(), entry);
  }

  private synchronized void expire() {
    Map<String, Entry> newCache = new HashMap<>(serviceCache);
    Map<String, Entry> entries = new HashMap<>();

    for (Map.Entry<String, Entry> entry : newCache.entrySet()) {
      long val = entry.getValue().createdAt + EXPIRY - System.currentTimeMillis();
      if (val > 0) {
        entries.put(entry.getKey(), entry.getValue());
      }
    }
    serviceCache = Collections.synchronizedMap(entries);
  }

  public List<ServiceDescriptor> getServices() {
    if (expiring) {
      expire();
    }
    return this.serviceCache.values().stream().map(val -> val.data).collect(Collectors.toList());
  }

  static class Entry {
    ServiceDescriptor data;
    long createdAt;

    Entry(InstanceDescriptor descriptor) {
      createdAt = System.currentTimeMillis();
      List<InstanceDescriptor> instances = new ArrayList<>();
      instances.add(descriptor);

      this.data = new ServiceDescriptor(descriptor.getIdentifier(),
        descriptor.getTags(), descriptor.getCodecs(),
        descriptor.getCapabilities(),
        instances);
    }

    void touch(InstanceDescriptor descriptor) {
      createdAt = System.currentTimeMillis();
      if (descriptor == null) {
        log.error("Got a null descriptor!");
      }
      data.getInstanceDescriptors().removeIf(integer -> integer.getInstanceId().equals(descriptor.getInstanceId()));
      data.getInstanceDescriptors().add(descriptor);
    }
  }
}
