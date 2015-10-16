package io.muoncore.transport;

import java.util.UUID;

/**
 * Cause the channel this is passed to to close and all connected resources to terminate as the pill
 * moves along the full channel. Expected to go through to the remote.
 */
public class PoisonPill extends TransportOutboundMessage {
    public PoisonPill(String serviceName, String protocol) {
        super(UUID.randomUUID().toString(), serviceName, protocol);
    }
}
