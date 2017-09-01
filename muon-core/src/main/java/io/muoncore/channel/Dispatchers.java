package io.muoncore.channel;

import io.muoncore.transport.client.RingBufferLocalDispatcher;

/**
 * Work dispatchers
 */
public class Dispatchers {

  private static Dispatcher RING = new Reactor2Dispatcher(new RingBufferLocalDispatcher("local"));
  private static Dispatcher WORKER = new PoolDispatcher();

  public static Dispatcher dispatcher() {
    return RING;
  }
  public static Dispatcher poolDispatcher() {
    return WORKER;
  }
}
