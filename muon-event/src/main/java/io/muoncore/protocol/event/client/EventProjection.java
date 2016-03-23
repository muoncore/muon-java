package io.muoncore.protocol.event.client;

import org.reactivestreams.Publisher;

public interface EventProjection<ProjectionType> {

    /**
     * Obtain the latest projection state.
     * This will come from local memory and be updated in the background.
     *
     * This is done in a non transactional way with the server side, and so has
     * eventually consistent semantics with the source event stream.
     */
    ProjectionType getCurrentState();

    /**
     * Allow direct subscription to versions of the projection as it is updated.
     * @return
     */
    Publisher<ProjectionType> projectionUpdates();

    /**
     * Stop receiving updates when this projection changes.
     */
    void cancelUpdate();
}

