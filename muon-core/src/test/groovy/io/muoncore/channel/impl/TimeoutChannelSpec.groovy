package io.muoncore.channel.impl

import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.transport.client.RingBufferLocalDispatcher
import reactor.Environment
import spock.lang.Specification

class TimeoutChannelSpec extends Specification {

    def "if channel does not have a message sent right for X, then it sends a timeout left"() {

        Environment.initializeIfEmpty()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new TimeoutChannel(dispatcher, 5000)
        MuonMessage timeoutmsg

        channel.left().receive {
            timeoutmsg = it
        }
        channel.right().receive {

        }
        when:
        channel.left().send(MuonMessageBuilder.fromService("Hello world").buildInbound())

        and:
        sleep(5000)

        channel.left().send(MuonMessageBuilder.fromService("Hello world").buildInbound())sudo 
        then:
        timeoutmsg != null

    }
}
