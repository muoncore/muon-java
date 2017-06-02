package io.muoncore;

import io.muoncore.channel.support.Scheduler;
import io.muoncore.protocol.introspection.client.IntrospectionClientProtocolStack;
import io.muoncore.transport.TransportControl;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        ServerRegistrarSource,
        IntrospectionClientProtocolStack{

        Scheduler getScheduler();
        void shutdown();
        TransportControl getTransportControl();
}
