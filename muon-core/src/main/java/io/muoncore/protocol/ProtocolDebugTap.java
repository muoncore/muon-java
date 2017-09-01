package io.muoncore.protocol;


import io.muoncore.Muon;
import io.muoncore.message.MuonMessage;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Taps all flowing MuonMessages and stores protocol traffic for later debug.
 */
@Slf4j
public class ProtocolDebugTap {

  public ProtocolDebugTap(Muon muon) {

    Publisher<MuonMessage> tap = muon.getTransportControl().tap(muonMessage -> true);

    tap.subscribe(new Subscriber<MuonMessage>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(MuonMessage muonMessage) {

      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onComplete() {
        log.info("Debug Tap has terminated. Not sure why ... ");
      }
    });
  }
}
