package io.muoncore.spring.repository;

/**
 * Standardised Event Store Repository.
 *
 * Provide access to whatever the currently active event store is via a common interface
 */
public interface MuonEventStoreRepository {

    /**
     * Emit an event onto the named stream
     * @param streamName
     * @param payload
     */
//    void event(String eventType, String streamName, Object payload);

    /**
     * Emit an event onto the default stream, named "general"
     * @param payload
     */
//    void event(String eventType, Object payload);

    /**
     * Emit an event, with a parent id, onto the named stream.
     * @param streamName
     * @param parentId The identifier of the event that logically <i>caused</i> this event to be emitted.
     *                 This relationship will not enforced by the event store.
     * @param payload The event itself.
     */
//    void event(String eventType, String streamName, String parentId, Object payload);
//
//    void replay(String streamName, EventReplayMode mode, Subscriber<Event> event) throws UnsupportedEncodingException, URISyntaxException;
}
