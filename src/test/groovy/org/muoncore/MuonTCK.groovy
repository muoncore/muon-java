package org.muoncore

import groovy.json.JsonBuilder
import org.eclipse.jetty.util.ajax.JSON
import org.muoncore.Muon
import org.muoncore.MuonBroadcastEvent
import org.muoncore.MuonBroadcastEventBuilder
import org.muoncore.MuonService
import org.muoncore.extension.amqp.AmqpTransportExtension
import org.muoncore.extension.http.HttpTransportExtension

class MuonTCK {
  
  
  static void main(args) {


    MuonService muon = new Muon()

    muon.serviceIdentifer = "tck"
    muon.registerExtension(new HttpTransportExtension(7171))
    muon.registerExtension(new AmqpTransportExtension())
    muon.start()

    List events = Collections.synchronizedList(new ArrayList())

    muon.receive("tckBroadcast") { MuonBroadcastEvent event ->
        events.add(JSON.parse(event.getPayload().toString()))
    }

    muon.onGet("/event", "Get The Events") {
        return JSON.toString(events)
    }

    muon.onDelete("/event", "Remove The Events") {
        events.clear()
        return [:]
    }

    muon.onGet("/echo", "Get Some Data") {
        Map obj = (Map) JSON.parse((String) it.getPayload())

        obj.put("method", "GET")

        return JSON.toString(obj)
    }

    muon.onPost("/echo", "Allow posting of some data") {
        Map obj = (Map) JSON.parse((String) it.getPayload())

        obj.put("method", "POST")

        return JSON.toString(obj)
    }

    muon.onPut("/echo", "Allow posting of some data") {
        //todo, something far more nuanced. Need to return a resource url as part of the creation.

        Map obj = (Map) JSON.parse((String) it.getPayload())

        obj.put("method", "PUT")

      muon.emit(MuonBroadcastEventBuilder.broadcast("analysis").withContent(new JsonBuilder([

      ]).toPrettyString()).build());

        return JSON.toString(obj)
    }

    muon.onDelete("/echo", "Allow deleting of some data") {

        Map obj = (Map) JSON.parse((String) it.getPayload())

        obj.put("method", "DELETE")


        return JSON.toString(obj)
    }
  }
  
}
