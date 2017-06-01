package io.muoncore;

import io.muoncore.channel.support.Scheduler;
import io.muoncore.protocol.introspection.client.IntrospectionClientProtocolStack;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerHandlerApi;
import io.muoncore.transport.TransportControl;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        ServerRegistrarSource,
        ReactiveStreamClientProtocolStack,
        ReactiveStreamServerHandlerApi,
        IntrospectionClientProtocolStack{

        Scheduler getScheduler();
        void shutdown();
        TransportControl getTransportControl();
}
