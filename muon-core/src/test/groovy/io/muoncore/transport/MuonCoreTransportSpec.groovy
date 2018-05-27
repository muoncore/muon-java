package io.muoncore.transport

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.descriptors.ServiceExtendedDescriptor
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MuonCoreTransportSpec extends Specification {


  def "can discover other services across the connection"() {

    Muon muon = MuonBuilder.withConfig(MuonConfigBuilder
      .withServiceIdentifier("testservice5")
      .build()).build()

    Muon muon2 = MuonBuilder.withConfig(MuonConfigBuilder
      .withServiceIdentifier("testservice2")
      .build()).build()

    println "WIll now test things"

    when:

    def latch = new CountDownLatch(1)
    List services

    muon2.discovery.onReady {
      services = muon.discovery.serviceNames
      latch.countDown()
    }

    latch.await(5, TimeUnit.SECONDS)

    then:
    services.size() > 0

    cleanup:
    muon.shutdown()
    muon2.shutdown()
  }

  def "can introspect other services across the connection"() {

    Muon muon = MuonBuilder.withConfig(MuonConfigBuilder
      .withServiceIdentifier("testservice1")
      .build()).build()

    Muon muon2 = MuonBuilder.withConfig(MuonConfigBuilder
      .withServiceIdentifier("testservice2")
      .build()).build()

    when:

    def latch = new CountDownLatch(1)
    ServiceExtendedDescriptor report

    muon2.discovery.onReady {
      report = muon.introspect("testservice1").get()
      latch.countDown()
    }

    latch.await(5, TimeUnit.SECONDS)

    println report

    then:
    report != null
    report.serviceName == "testservice1"

    cleanup:
    muon.shutdown()
    muon2.shutdown()
  }
}
