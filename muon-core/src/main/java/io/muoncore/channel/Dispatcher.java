package io.muoncore.channel;

import java.util.function.Consumer;

public interface Dispatcher {

  <E> void dispatch(E data,
                    Consumer<E> eventConsumer,
                    Consumer<Throwable> errorConsumer);

  <E> void tryDispatch(E data,
                       Consumer<E> eventConsumer,
                       Consumer<Throwable> errorConsumer);
}
