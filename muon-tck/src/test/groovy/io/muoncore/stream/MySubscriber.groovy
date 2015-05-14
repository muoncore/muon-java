package io.muoncore.stream

import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Created by david on 14/05/15.
 */
class MySubscriber implements Subscriber {
  def errors

  @Override
  void onSubscribe(Subscription s) {
    println "Subscribed"
  }

  @Override
  void onNext(Object o) {

  }

  @Override
  void onError(Throwable t) {
    println "Error was thrown: '${t.message}'"
    t.printStackTrace()
    errors << t
  }

  @Override
  void onComplete() {
    println "Subscriber is now completed"
  }
}
