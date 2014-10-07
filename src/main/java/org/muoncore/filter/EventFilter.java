package org.muoncore.filter;

import org.muoncore.MuonEvent;

public interface EventFilter {
    public boolean canHandle(MuonEvent event);
}
