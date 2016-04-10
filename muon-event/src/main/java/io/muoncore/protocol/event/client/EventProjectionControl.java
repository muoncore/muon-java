package io.muoncore.protocol.event.client;

public interface EventProjectionControl<ProjectionType> {

    /**
     * Obtain the latest projection state.
     * This will come from local memory and be updated in the background.
     *
     * This is done in a non transactional way with the server side, and so has
     * eventually consistent semantics with the source event stream.
     */
    ProjectionType getCurrentState();


}

