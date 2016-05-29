package io.muoncore;

import io.muoncore.protocol.introspection.client.IntrospectionClientProtocolStack;
import io.muoncore.protocol.reactivestream.client.ReactiveStreamClientProtocolStack;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerHandlerApi;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack;
import io.muoncore.protocol.requestresponse.server.RequestResponseHandlersSource;
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandlerApi;
import io.muoncore.transport.TransportControl;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        RequestResponseHandlersSource,
        RequestResponseClientProtocolStack,
        RequestResponseServerHandlerApi,
        ReactiveStreamClientProtocolStack,
        ReactiveStreamServerHandlerApi,
        IntrospectionClientProtocolStack{

        void shutdown();
        TransportControl getTransportControl();
}
