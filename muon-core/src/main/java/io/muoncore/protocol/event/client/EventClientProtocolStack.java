package io.muoncore.protocol.event.client;

public interface EventClientProtocolStack  {

    /**
     * Provide a client to the remote event store. Permitting rich interactions with persisted stream data
     */
    EventStoreClient getEventStoreClient();
}
