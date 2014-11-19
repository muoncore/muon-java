package org.muoncore.extension.amqp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ServiceCache {

    static final int EXPIRY = 5000;

    private Map<String, Long> serviceCache = new HashMap<String, Long>();

    public void addService(String id) {
        serviceCache.put(id, System.currentTimeMillis());
    }

    private synchronized void expire() {
        Map<String, Long> entries = new HashMap<String, Long>();
        for (Map.Entry<String, Long> entry: serviceCache.entrySet()) {
            long val = entry.getValue() + EXPIRY - System.currentTimeMillis();
            if (val > 0) {
                entries.put(entry.getKey(), entry.getValue());
            }
        }
        serviceCache = entries;
    }

    public Set<String> getServiceIds() {
        expire();
        return serviceCache.keySet();
    }
}