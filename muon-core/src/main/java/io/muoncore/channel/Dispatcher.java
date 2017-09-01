package io.muoncore.channel;

import java.util.function.Consumer;

public interface Dispatcher {




  /**
   *  Dispatch onto the event dispatch thread.
   *
   *  Guarantees order, is by far the fastest dispatcher.
   *
   *  Do not block or do long running work on this thread, or it will be terminated with prejudice. You have a max of 15ms to do your stuff, or use some other dispatch.
   * @param data
   * @param eventConsumer
   * @param errorConsumer
   * @param <E>
   */
  <E> void dispatch(E data,
                    Consumer<E> eventConsumer,
                    Consumer<Throwable> errorConsumer);

  <E> void tryDispatch(E data,
                       Consumer<E> eventConsumer,
                       Consumer<Throwable> errorConsumer);
}
