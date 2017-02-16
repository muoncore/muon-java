package io.muoncore.protocol.event.client;


import io.muoncore.Muon;
import io.muoncore.api.MuonFuture;
import io.muoncore.protocol.event.Event;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class AggregateEventClient extends DefaultEventClient {


  public AggregateEventClient(Muon muon) {
    super(muon);
  }

  public void loadAggregateRoot(String id) {
    MuonFuture<EventReplayControl> control = replay("aggregate/" + id, EventReplayMode.REPLAY_ONLY, new Subscriber<Event>() {
      @Override
      public void onSubscribe(Subscription s) {

      }

      @Override
      public void onNext(Event o) {

      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onComplete() {

      }
    });
  }
}
