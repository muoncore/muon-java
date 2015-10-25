package io.muoncore;

import io.muoncore.protocol.event.client.EventClientProtocolStack;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack;
import io.muoncore.protocol.requestresponse.server.RequestResponseHandlersSource;
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandlerApi;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        EventClientProtocolStack,
        RequestResponseHandlersSource,
        RequestResponseClientProtocolStack,
        RequestResponseServerHandlerApi {

        void shutdown();
}
