package io.muoncore.protocol.event.client;

import io.muoncore.api.MuonFuture;
import io.muoncore.protocol.event.ClientEvent;
import io.muoncore.protocol.event.Event;
import org.reactivestreams.Subscriber;

import java.util.List;

public interface EventClient {

    /**
     * Emit an event into the remote event store.
     * @param event
     * @param <X>
     * @return
     */
    <X> EventResult event(ClientEvent<X> event);

    /**
     * Load an event by id
     */
//    <X> MuonFuture<Event<X>> loadEvent(String id, Class<X> type);

    /**
     * Replay an event stream, allowing the creation of an aggregated data structure (a reduction or projection)
     * Or serial processing of the stream.
     *
     * This method requires an event store to be active in the distributed system. If one is not active, a MuonException
     * will be thrown.
     *
     * This will optionally replay from the start of the stream up to the current and then switch to HOT processing for all messages
     * after this.
     *
     * @param streamName The name of the stream to be replayed
     * @param mode Whether to replay just the future data, or request to load historical data, if supported on the remote stream
     * @param subscriber The reactive streams subscriber that will listen to the event stream.
     */
    void replay(String streamName, EventReplayMode mode, Subscriber<Event> subscriber);


    /**
     * Emit an event into the remote event store.
     */
//    <X> MuonFuture<EventNode> loadChain(String eventId);

    <X> MuonFuture<List<EventProjectionDescriptor>> getProjectionList();

    <X> MuonFuture<EventProjectionControl<X>> getProjection(String name, Class<X> type);
}
