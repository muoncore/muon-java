package org.muoncore.filter;

import org.muoncore.transports.MuonMessageEvent;

public interface EventFilter {
    public boolean canHandle(MuonMessageEvent event);
}
