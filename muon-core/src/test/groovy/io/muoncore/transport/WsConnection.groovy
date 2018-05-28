package io.muoncore.transport

import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.ServiceDescriptor
import io.muoncore.api.MuonFuture
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.descriptors.ServiceExtendedDescriptor
import io.muoncore.protocol.Auth
import io.muoncore.protocol.event.ClientEvent
import io.muoncore.protocol.event.client.DefaultEventClient
import io.muoncore.protocol.event.client.EventReplayMode
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription


def config = MuonConfigBuilder
  .withServiceIdentifier("awesome")
  .build()

Muon muon = MuonBuilder.withConfig(config).build()

def ev = new DefaultEventClient(muon)
def auth = new Auth("aether", "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjFjY2U0NWExLTQzYTUtNDdjNS1hMDNmLTA4ZDgyNWQ1YzJlZSIsIndvcmtzcGFjZUlkIjoiMjQxNzg3YTgtZTJjMC00ZDJiLWE5NTAtMjRhZThhOGNiOTU4IiwidHlwZSI6InVzZXIiLCJncmFudCI6WyJ3b3Jrc3BhY2VfYWRtaW4iXSwidXNlcm5hbWUiOiJtZUBkYXZpZGRhd3Nvbi5tZSIsImdyYW50cyI6W119.J_a2DROvcH_6CmVyX79x0Olks_ACxGLFVVN9sBL39-1hBkf8nJHHJ3mI3Dy6nG0ggLINARiEhpPuwMnE0dS7Yzr9UQSNKNZ46IpTqjwFvwjrJk7ch6Vmg15xTR2dGXFnattijAyxZ6c9zz317w9ewxfnG-dd2qeOgQBghpKDCs9cCp1HA2NQpQ-bpB5oBQlcEibBcAYv2J-Pb417Bw3z3GD6bvhKYqttInj9Tp4I6lyycA_gUqXu3VRdnjFOLF5r-u1RqvtMk_rR9rzOOEnVuqfM0Cm0KjWKKc7k1aPo9hjZrkDWUb0fN_YRu4ZUmc7jphDBraPp0Ui4OReq_ZhJosy9ypf9fxRn-vL-Lt00qGIdgTbkaD47dCZQfI9YWlc5u9BOgpzDrvJ_Yws8-Uh2zbWsrIvMfRl15jXZ-2ZwPi8WNI7mgvz4sJm__nvAy9ubYCTlC0tu8kgfVmvpvnNVqlbfNdSFDSvM2zomViHY6JgNaVru5yX1h9LdxWvAx2tSB2tQOxnzJzhhBHH-4P160Req4wbYfH1RPfeyqxHAaKmjUyaXpQfhpDSJ5KOU4c4KjN1kgEmrFeAkEDxSqIbubdvLT_ieOpdm6sCT8p8FUgRUF0L7rTZrZ1yeZGBojUQ4t8pTKw1gu-CFuD9qmgRSvxn4dxpJGJadla8J2E4SE1Y")


muon.discovery.onReady {
  println "AWESOME"
  println "SERVICES ARE $muon.discovery.serviceNames}"

  def introspect = muon.introspect("photonlite").get()
  println "INTROSPECTION"
  println introspect

  println ev.event(
    ClientEvent.ofType("SomethingHappened")
      .stream("awesome")
      .payload(["happy": "clappy"])
      .build(), auth)


  ev.replay("awesome", auth, EventReplayMode.REPLAY_ONLY, new Subscriber() {

    @Override
    void onSubscribe(Subscription s) {
      s.request(100)
    }

    @Override
    void onNext(Object o) {
      println "GOT DATA ${o}"
    }

    @Override
    void onError(Throwable t) {
      t.printStackTrace()
    }

    @Override
    void onComplete() {
      println "STREAM FINISHED"
    }
  })

  ev.replay("awesome", auth, EventReplayMode.REPLAY_ONLY, new Subscriber() {

    @Override
    void onSubscribe(Subscription s) {
      s.request(100)
    }

    @Override
    void onNext(Object o) {
      println "2222222GOT DATA ${o}"
    }

    @Override
    void onError(Throwable t) {
      t.printStackTrace()
    }

    @Override
    void onComplete() {
      println "222222STREAM FINISHED"
    }
  })

  sleep(10000)
  muon.shutdown()
}

