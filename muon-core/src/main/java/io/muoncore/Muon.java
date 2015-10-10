package io.muoncore;

import io.muoncore.protocol.event.client.EventClientProtocolStack;
import io.muoncore.protocol.requestresponse.client.RequestResponseClientProtocolStack;

/**
 * Default set of protocol stacks.
 */
public interface Muon extends
        EventClientProtocolStack,
        RequestResponseClientProtocolStack {

}
