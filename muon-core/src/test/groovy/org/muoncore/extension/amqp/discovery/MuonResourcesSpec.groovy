package org.muoncore.extension.amqp.discovery

import org.muoncore.Discovery
import org.muoncore.Muon
import org.muoncore.transports.MuonResourceEventBuilder
import spock.lang.Specification

class MuonResourcesSpec extends Specification {

  def "GET on a non existent service gives a 404 response"() {
    given:
    def discovery = Mock(Discovery) {
      getService(_) >> null
    }

    def muon = new Muon(discovery)

    when:
    def ret = muon.get("muon://borked/hello")

    then:
    ret.success == false
    ret.responseEvent.resource == "/hello"
    ret.responseEvent.serviceId == "borked"
    ret.responseEvent.headers.status == "404"
  }

  def "PUT on a non existent service gives a 404 response"() {
    given:
    def discovery = Mock(Discovery) {
      getService(_) >> null
    }

    def muon = new Muon(discovery)

    when:
    def ret = muon.put("muon://borked/hello", MuonResourceEventBuilder.event("").build())

    then:
    ret.success == false
    ret.responseEvent.resource == "/hello"
    ret.responseEvent.serviceId == "borked"
    ret.responseEvent.headers.status == "404"
  }

  def "POST on a non existent service gives a 404 response"() {
    given:
    def discovery = Mock(Discovery) {
      getService(_) >> null
    }

    def muon = new Muon(discovery)

    when:
    def ret = muon.post("muon://borked/hello", MuonResourceEventBuilder.event("").build())

    then:
    ret.success == false
    ret.responseEvent.resource == "/hello"
    ret.responseEvent.serviceId == "borked"
    ret.responseEvent.headers.status == "404"
  }

  def "DELETE on a non existent service gives a 404 response"() {
    given:
    def discovery = Mock(Discovery) {
      getService(_) >> null
    }

    def muon = new Muon(discovery)

    when:
    def ret = muon.post("muon://borked/hello", MuonResourceEventBuilder.event("").build())

    then:
    ret.success == false
    ret.responseEvent.resource == "/hello"
    ret.responseEvent.serviceId == "borked"
    ret.responseEvent.headers.status == "404"
  }
}
