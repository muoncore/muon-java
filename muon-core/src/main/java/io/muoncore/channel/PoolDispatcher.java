package io.muoncore.channel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PoolDispatcher implements Dispatcher {

  private Executor pool = Executors.newCachedThreadPool();

  @Override
  public <E> void dispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
    pool.execute(() -> {
      try {
        eventConsumer.accept(data);
      } catch (Throwable e) {
        errorConsumer.accept(e);
      }
    });
  }

  @Override
  public <E> void tryDispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
    dispatch(data, eventConsumer, errorConsumer);
  }
}
