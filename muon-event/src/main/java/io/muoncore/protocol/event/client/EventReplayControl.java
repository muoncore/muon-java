package io.muoncore.protocol.event.client;

import io.muoncore.api.MuonFuture;

/**
 * Give information around an event replay.
 */
public interface EventReplayControl {
    /**
     *
     */
    int getColdEventCount();

    /**
     * Cancel this stream listener.
     * Same effect as cancel on the reactive stream subscription
     */
    void cancelReplay();

    /**
     * When ReplayMode.COLD_HOT, this will be notified when the last COLD event has been presented.
     */
    MuonFuture<ReplayComplete> notifyOnColdFullyConsumed();

    interface ReplayComplete {
        /**
         * The number of events that were replayed
         */
        int getColdReplayedEventCount();

        /**
         * The time taken for the cold stream replay to have taken place.
         */
        long getReplayTime();
    }
}
