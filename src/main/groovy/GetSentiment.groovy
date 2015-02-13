import groovy.json.JsonBuilder
import org.muoncore.Muon
import org.muoncore.extension.amqp.AmqpTransportExtension
import org.muoncore.extension.amqp.discovery.AmqpDiscovery
import org.muoncore.transports.MuonResourceEventBuilder

/**
 * Created by david on 09/02/15.
 */


final Muon muon = new Muon(
    new AmqpDiscovery("amqp://localhost:5672"));
muon.registerExtension(new AmqpTransportExtension());

muon.setServiceIdentifer("sentimentconsumer");
muon.start();

Thread.sleep(5000)

def payload = new JsonBuilder([text:"I am a happy cloud"]).toPrettyString()

def ret = muon.get(
    MuonResourceEventBuilder.textMessage(payload)
    .withUri("muon://sentinel/").build())

def data = ret.event.payload.toString();

System.out.println ("We had data " + data);
