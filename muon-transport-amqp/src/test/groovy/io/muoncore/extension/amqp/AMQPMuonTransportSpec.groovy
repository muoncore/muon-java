package io.muoncore.extension.amqp

import spock.lang.Specification

class AMQPMuonTransportSpec extends Specification {

    /*

    outgoing connection
        initially, just a normal channel.
        when an outgoing message sent, channel established to remote/proto

    incoming connection, handshake and establish channel over AMQP, connect to ServerStacks

    */

    def "be awesome"() {

    }

}
