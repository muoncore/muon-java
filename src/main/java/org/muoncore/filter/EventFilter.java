package org.muoncore.filter;

import org.muoncore.MuonBroadcastEvent;

public interface EventFilter {
    public boolean canHandle(MuonBroadcastEvent event);
}
