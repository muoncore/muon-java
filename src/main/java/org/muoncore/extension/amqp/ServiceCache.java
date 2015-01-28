package org.muoncore.extension.amqp;

import java.util.*;

class ServiceCache {

    static final int EXPIRY = 5000;

    private Map<String, Entry> serviceCache = new HashMap<String, Entry>();

    public void addService(Map service) {
        String id = (String) service.get("identifier");

        serviceCache.put(id, new Entry(service, System.currentTimeMillis()));
    }

    private synchronized void expire() {
        Map<String, Entry> entries = new HashMap<String, Entry>();
        for (Map.Entry<String, Entry> entry: serviceCache.entrySet()) {
            long val = entry.getValue().createdAt + EXPIRY - System.currentTimeMillis();
            if (val > 0) {
                entries.put(entry.getKey(), entry.getValue());
            }
        }
        serviceCache = entries;
    }

    public List<Map> getServices() {
        expire();
        List<Map> services = new ArrayList<Map>();

        for(Entry entry: serviceCache.values()) {
            services.add(entry.data);
        }
        return services;
    }

    static class Entry {
        Map data;
        long createdAt;

        public Entry(Map data, long createdAt) {
            this.data = data;
            this.createdAt = createdAt;
        }
    }
}