package io.muoncore.transport

import io.muoncore.InstanceDescriptor
import io.muoncore.ServiceDescriptor
import spock.lang.Specification

class ServiceCacheSpec extends Specification {

  def "service cache contains the data put into it"() {

    when:
    def cache = new ServiceCache()
    cache.addService(service("simpleservice"))
    cache.addService(service("simpleservice2"))
    cache.addService(service("simpleservice3"))

    then:
    cache.services.size() == 3
    cache.services.find { it.identifier == "simpleservice"}
    cache.services.find { it.identifier == "simpleservice2"}
    cache.services.find { it.identifier == "simpleservice3"}

  }

  def "service cache expires"() {
    given:
    def cache = new ServiceCache()
    cache.addService(service("simpleservice"))
    cache.addService(service("simpleservice2"))
    cache.addService(service("simpleservice3"))

    when:
    Thread.sleep(5100)

    then:
    cache.services.size() == 0
  }

  def service(name) {
    new InstanceDescriptor("$name-instance", name, ["tag"], ["application/json"], [new URI("some://hello")], [])
  }
}
