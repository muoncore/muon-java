package org.muoncore.extension.amqp

import spock.lang.Specification

class ServiceCacheSpec extends Specification {

  def "service cache contains the data put into it"() {

    when:
    def cache = new ServiceCache()
    cache.addService("simpleservice")
    cache.addService("simpleservice2")
    cache.addService("simpleservice3")

    then:
    cache.serviceIds.size() == 3
    cache.serviceIds.contains("simpleservice")
    cache.serviceIds.contains("simpleservice2")
    cache.serviceIds.contains("simpleservice3")

  }

  def "service cache expires"() {
    given:
    def cache = new ServiceCache()
    cache.addService("simpleservice")
    cache.addService("simpleservice2")
    cache.addService("simpleservice3")

    when:
    Thread.sleep(5100)

    then:
    cache.serviceIds.size() == 0
  }
}
