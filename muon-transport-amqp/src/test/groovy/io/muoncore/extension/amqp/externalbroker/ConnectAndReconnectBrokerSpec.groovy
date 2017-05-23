package io.muoncore.extension.amqp.externalbroker

import com.rabbitmq.client.Channel
import io.muoncore.InstanceDescriptor
import io.muoncore.MultiTransportMuon
import io.muoncore.Muon
import io.muoncore.channel.impl.StandardAsyncChannel
import io.muoncore.channel.support.Scheduler
import io.muoncore.codec.json.JsonOnlyCodecs
import io.muoncore.config.AutoConfiguration
import io.muoncore.extension.amqp.*
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory
import io.muoncore.memory.discovery.InMemDiscovery
import io.muoncore.protocol.DynamicRegistrationServerStacks
import io.muoncore.protocol.ServerStacks
import io.muoncore.protocol.defaultproto.DefaultServerProtocol
import io.muoncore.protocol.reactivestream.server.PublisherLookup
import io.muoncore.transport.client.SimpleTransportMessageDispatcher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import reactor.Environment
import reactor.rx.broadcast.Broadcaster
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.util.concurrent.PollingConditions

@IgnoreIf({ System.getenv("SHORT_TEST") })
class ConnectAndReconnectBrokerSpec extends BaseEmbeddedBrokerSpec {

  @Shared
  def discovery = new InMemDiscovery()

  @AutoCleanup("shutdown")
  @Shared
  AMQPMuonTransport transport

  @Shared
  ServerStacks serverStacks = new DynamicRegistrationServerStacks(new DefaultServerProtocol(null, null, null), new SimpleTransportMessageDispatcher())

  def setupSpec() {
    transport = muon("simples")
    discovery.advertiseLocalService(new InstanceDescriptor("123", "simples", [], ["application/json"], [new URI("amqp://muon:microservices@localhost")], []))
  }

  def "will reconnect to a broker after connecting and the broker failing"() {
    given: "An open connection to a broker"
    def env = Environment.initializeIfEmpty()

    when: "The broker fails"
    rabbitMq.stop()
    Thread.sleep(5000)

    and: "The broker restarts"
    rabbitMq.start()

    then: "The transport will reconnect"

    new PollingConditions(timeout: 30).eventually {
      try {
        transport.openClientChannel("simples", "rpc")
      } catch(Exception e) {
        println "Failed to connect ... ${e.message}"
        false
      }
    }

  }

  private AMQPMuonTransport muon(serviceName) {

    def connection = new RabbitMq09ClientAmqpConnection("amqp://muon:microservices@localhost")
    def serviceQueue = new DefaultServiceQueue(serviceName, connection)
    def channelFactory = new DefaultAmqpChannelFactory(serviceName, new RabbitMq09QueueListenerFactory(connection.getChannel()), connection)

    def ret =  new AMQPMuonTransport(
      "amqp://muon:microservices@localhost", serviceQueue, channelFactory)
    ret.start(discovery, serverStacks, new JsonOnlyCodecs(), new Scheduler())
    return ret
  }

}
