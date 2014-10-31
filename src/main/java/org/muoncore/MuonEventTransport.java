package org.muoncore;

import java.util.List;

/**
 * A connector between Muon and the world.
 * Java implementation specific, don't expect this concept to work the same in other
 * languages.
 *
 * The responsibility here is to translate between the Muon model (MuonBroadcastEvent, MuonResourceEvent etc)
 * and what is necessary tro transfer this over the wire.
 */
public interface MuonEventTransport {

    public List<ServiceDescriptor> discoverServices();

    public void shutdown();

    public void start();
}
