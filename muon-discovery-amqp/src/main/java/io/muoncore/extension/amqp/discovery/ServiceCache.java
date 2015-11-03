package io.muoncore.extension.amqp.discovery;

import io.muoncore.ServiceDescriptor;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceCache {

    static final int EXPIRY = 5000;

    private Map<String, Entry> serviceCache = new HashMap<>();

    public void addService(ServiceDescriptor service) {
        serviceCache.put(
                service.getIdentifier(),
                new Entry(service, System.currentTimeMillis()));
    }

    private synchronized void expire() {
        Map<String, Entry> entries = new HashMap<>();
        for (Map.Entry<String, Entry> entry: serviceCache.entrySet()) {
            long val = entry.getValue().createdAt + EXPIRY - System.currentTimeMillis();
            if (val > 0) {
                entries.put(entry.getKey(), entry.getValue());
            }
        }
        serviceCache = entries;
    }

    public List<ServiceDescriptor> getServices() {
        expire();
        return this.serviceCache.values().stream().map( val -> val.data).collect(Collectors.toList());
    }

    static class Entry {
        ServiceDescriptor data;
        long createdAt;

        public Entry(ServiceDescriptor data, long createdAt) {
            this.data = data;
            this.createdAt = createdAt;
        }
    }
}