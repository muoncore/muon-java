package io.muoncore.channel.impl

import io.muoncore.channel.Reactor2Dispatcher
import io.muoncore.channel.support.Scheduler
import io.muoncore.message.MuonMessage
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.transport.client.RingBufferLocalDispatcher
import reactor.Environment
import spock.lang.Specification

class TimeoutChannelSpec extends Specification {

    def "if channel does not have a message sent right for X, then it sends a timeout left"() {

        Environment.initializeIfEmpty()
        def sched = new Scheduler()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new TimeoutChannel(new Reactor2Dispatcher(dispatcher), sched, 5000)
        MuonMessage timeoutmsg

        channel.left().receive {
            timeoutmsg = it
        }
        channel.right().receive {
            println "Got data on the right."
        }

        channel.left().send(MuonMessageBuilder.fromService("hello").build())

        when:
        sleep(5100)

        then:
        timeoutmsg != null
    }
}
