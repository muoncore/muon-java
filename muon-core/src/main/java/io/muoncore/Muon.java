package io.muoncore;

import io.muoncore.channel.support.Scheduler;
import io.muoncore.codec.CodecsSource;
import io.muoncore.config.MuonConfigurationSource;
import io.muoncore.protocol.introspection.client.IntrospectionClientProtocolStack;
import io.muoncore.transport.TransportClientSource;
import io.muoncore.transport.TransportControl;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        ServerRegistrarSource,
        IntrospectionClientProtocolStack,
        TransportClientSource, CodecsSource, MuonConfigurationSource, DiscoverySource {

        Scheduler getScheduler();
        void shutdown();
        TransportControl getTransportControl();
}
