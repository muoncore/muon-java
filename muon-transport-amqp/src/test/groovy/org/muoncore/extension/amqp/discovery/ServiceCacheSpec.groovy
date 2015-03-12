package org.muoncore.extension.amqp.discovery

import org.muoncore.extension.amqp.discovery.ServiceCache
import spock.lang.Specification

class ServiceCacheSpec extends Specification {

  def "service cache contains the data put into it"() {

    when:
    def cache = new ServiceCache()
    cache.addService([identifier:"simpleservice"])
    cache.addService([identifier:"simpleservice2"])
    cache.addService([identifier:"simpleservice3"])

    then:
    cache.services.size() == 3
    cache.services.find { it.identifier == "simpleservice"}
    cache.services.find { it.identifier == "simpleservice2"}
    cache.services.find { it.identifier == "simpleservice3"}

  }

  def "service cache expires"() {
    given:
    def cache = new ServiceCache()
    cache.addService([identifier:"simpleservice"])
    cache.addService([identifier:"simpleservice2"])
    cache.addService([identifier:"simpleservice3"])

    when:
    Thread.sleep(5100)

    then:
    cache.services.size() == 0
  }
}
