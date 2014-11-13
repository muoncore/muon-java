package org.muoncore

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.muoncore.extension.amqp.AmqpTransportExtension

class SentimentalCheck {

  static void main(args) {

    def client = new Muon()
    client.registerExtension(new AmqpTransportExtension())
    client.start()

    def response = client.get(
        MuonResourceEventBuilder
            .textMessage(new JsonBuilder([text:"You really upset me, I can't speak now I'm so sad"]).toPrettyString())
            .withUri("muon://sentinel/sentiment")
            .build())

    println "Got Sentiment ${new JsonSlurper().parseText(response.responseEvent.payload).aggregateSentiment}"

    client.shutdown()

  }
}
