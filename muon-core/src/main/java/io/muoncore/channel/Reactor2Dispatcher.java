package io.muoncore.channel;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@AllArgsConstructor
@Slf4j
class Reactor2Dispatcher implements Dispatcher {

  private reactor.core.Dispatcher internal;

  @Override
  public <E> void dispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
//    RuntimeException ex = new RuntimeException();

    //todo, only capture if needed. can we remove entirely?

    internal.dispatch(data, e -> {
//      CountDownLatch latch = new CountDownLatch(1);
//      exec.execute(() -> {
//        try {
//          if (!latch.await(200, TimeUnit.MILLISECONDS)) {
//            log.error("Dispatch took too long! {}", data, ex);
//          }
//        } catch (InterruptedException e1) {
//
//          e1.printStackTrace();
//        }
//      });
      long then = System.currentTimeMillis();
      eventConsumer.accept(e);
//      latch.countDown();
//      long now = System.currentTimeMillis();
//      if (now - then > 20) {
//        log.warn("Event dispatch is running slowly {}, {}",now-then, data, ex);
//      }
    }, errorConsumer::accept);
  }

  @Override
  public <E> void tryDispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
    internal.tryDispatch(data, eventConsumer::accept, errorConsumer::accept);
  }
}
