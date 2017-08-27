package io.muoncore.channel;

import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class Reactor2Dispatcher implements Dispatcher {

  private reactor.core.Dispatcher internal;

  @Override
  public <E> void dispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
    internal.dispatch(data, eventConsumer::accept, errorConsumer::accept);
  }

  @Override
  public <E> void tryDispatch(E data, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
    internal.tryDispatch(data, eventConsumer::accept, errorConsumer::accept);
  }
}
