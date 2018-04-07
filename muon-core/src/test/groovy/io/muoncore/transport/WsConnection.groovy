package io.muoncore.transport

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.ServiceDescriptor
import io.muoncore.config.MuonConfigBuilder


def config = MuonConfigBuilder
  .withServiceIdentifier("awesome")
  .build()

Muon muon = MuonBuilder.withConfig(config).build()

muon.discovery.onReady {
  println "AWESOME"
  println "SERVICES ARE $muon.discovery.serviceNames}"

  def named = muon.discovery.getCodecsForService("photonlite")

  println "Named = ${named}"

  muon.shutdown()
}

