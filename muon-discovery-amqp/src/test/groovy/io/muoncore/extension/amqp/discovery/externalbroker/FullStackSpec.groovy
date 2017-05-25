package io.muoncore.extension.amqp.discovery.externalbroker

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommand
import io.muoncore.Muon
import io.muoncore.MuonBuilder
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.config.MuonConfigBuilder
import io.muoncore.message.MuonMessage
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import reactor.Environment
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Timeout

@Ignore("Relies on RPC, need to ")
@IgnoreIf({ System.getenv("SHORT_TEST") })
@Timeout(60)
class FullStackSpec extends Specification {

  /*
  @AutoCleanup("stop")
  EmbeddedRabbitMq rabbitMq

  EmbeddedRabbitMqConfig config

  def setup() {
    config = new EmbeddedRabbitMqConfig.Builder()
      .build();
    rabbitMq = new EmbeddedRabbitMq(config);
    rabbitMq.start()

    sleep(3000)
    cmd "rabbitmqctl", "add_user", "muon", "microservices"
    cmd "rabbitmqctl", "set_user_tags", "muon", "administrator"
    cmd "rabbitmqctl", "set_permissions", "-p", "/", "muon", '.*', '.*', '.*'
  }

  def "full amqp based stack works"() {

    Environment.initializeIfEmpty()
    StandardAsyncChannel.echoOut = true

    def svc1 = createMuon("simples")
    def svc2 = createMuon("tombola1")
    def svc3 = createMuon("tombola2")
    def svc4 = createMuon("tombola3")
    def svc5 = createMuon("tombola4")
    def svc6 = createMuon("tombola5")

    println "HELLO"

    svc2.handleRequest(all()) {
      it.answer(new ServerResponse(200, [hi: "there"]))
    }
    testTap(svc1) {
      println "Simples Tap ${it}"
    }
    testTap(svc2) {
      println "Tombola Tap ${it}"
    }

    when:
    Thread.sleep(3500)
    def then = System.currentTimeMillis()
//        def response = svc1.request("request://tombola1/hello", [hello:"world"]).get(1500, TimeUnit.MILLISECONDS)
    def response = svc1.request("request://tombola1/hello", [hello: "world"]).get()
    def now = System.currentTimeMillis()

    println "Latency = ${now - then}"
//        def discoveredServices = svc3.discovery.knownServices

    then:
//        discoveredServices.size() == 6
    response != null
    response.status == 200
    response.getPayload(Map).hi == "there"

    cleanup:
    StandardAsyncChannel.echoOut = false
    svc1.shutdown()
    svc2.shutdown()
    svc3.shutdown()
    svc4.shutdown()
    svc5.shutdown()
    svc6.shutdown()
  }

  private Muon createMuon(serviceName) {

    def config = MuonConfigBuilder.withServiceIdentifier(serviceName).build()

    println "Creating muon"

    MuonBuilder.withConfig(config).build()
  }

  def testTap(muon, Closure output) {
    muon.transportControl.tap({ true }).subscribe(new Subscriber<MuonMessage>() {
      @Override
      void onSubscribe(Subscription s) {
        s.request(500)
      }

      @Override
      void onNext(MuonMessage transportMessage) {
        output(transportMessage)
      }

      @Override
      void onError(Throwable t) {
        t.printStackTrace()
      }

      @Override
      void onComplete() {
        println "Tap complete"
      }
    })
  }

  def cmd(String cmd, String...args) {
    println "EXEC: $cmd $args"
    RabbitMqCommand command = new RabbitMqCommand(config, cmd, args);
    StartedProcess process = command.call();
    ProcessResult result = process.getFuture().get();
    println result.outputString()
  }*/
}
