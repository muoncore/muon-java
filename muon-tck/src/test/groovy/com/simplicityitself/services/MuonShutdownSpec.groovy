package com.simplicityitself.services

import io.muoncore.Muon
import io.muoncore.future.MuonFutures
import io.muoncore.extension.amqp.AmqpTransportExtension
import io.muoncore.extension.amqp.discovery.AmqpDiscovery
import spock.lang.Specification

class MuonShutdownSpec extends Specification {

  def "Muon can be cleanly shut down and replaced in a single process"() {

    given:
    def discovery1 = new AmqpDiscovery("amqp://muon:microservices@localhost")
    def muon1 = new Muon(discovery1)
    muon1.serviceIdentifer = "muonservice"
    new AmqpTransportExtension("amqp://muon:microservices@localhost").extend(muon1)

    muon1.start()

    muon1.onGet("/hello", Map) {
      return MuonFutures.immediately([message:"You are awesome"])
    }

    Thread.sleep(3000)

    when:"muonservice is shutdown"

    muon1.shutdown()
    Thread.sleep(2000)

    and:"a new muonservice is started"

    def discovery2 = new AmqpDiscovery("amqp://muon:microservices@localhost")
    def muon2 = new Muon(discovery2)
    muon2.serviceIdentifer = "muonservice"
    new AmqpTransportExtension("amqp://muon:microservices@localhost").extend(muon2)
    muon2.start()

    muon2.onGet("/hello", Map){
      return MuonFutures.immediately([message:"second service"])
    }


    Thread.sleep(3000)

    and: "A request is made"

    def ret = muon2.get("muon://muonservice/hello", Map)

    then: "The request is handled by the new muon"

    ret.get().success
    ret.get().responseEvent.decodedContent.message == "second service"
  }
}
