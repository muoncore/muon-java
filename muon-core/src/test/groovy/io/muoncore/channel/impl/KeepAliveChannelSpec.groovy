package io.muoncore.channel.impl

import io.muoncore.channel.Reactor2Dispatcher
import io.muoncore.channel.support.Scheduler
import io.muoncore.message.MuonMessageBuilder
import io.muoncore.transport.TransportEvents
import io.muoncore.transport.client.RingBufferLocalDispatcher
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class KeepAliveChannelSpec extends Specification {

    def "if channel does receive a message going left within the timeout period, then it sends a timeout left"() {

        Environment.initializeIfEmpty()
        def sched = new Scheduler()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new KeepAliveChannel(new Reactor2Dispatcher(dispatcher), "fakeproto", sched)
        def timeoutmsg = []

        channel.left().receive {
            println "Got data left"
            timeoutmsg << it
        }
        channel.right().receive {
            println "Got data on the right."
        }

        when:
        sleep(7000)

        then:
        new PollingConditions(timeout: 5).eventually {
            timeoutmsg.size() == 1
            timeoutmsg[0].step == TransportEvents.CONNECTION_FAILURE
        }
    }

    def "service sends keep alive pings"() {

        Environment.initializeIfEmpty()
        def sched = new Scheduler()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new KeepAliveChannel(new Reactor2Dispatcher(dispatcher), "fakeproto", sched)
        def timeoutmsg = []

        channel.left().receive {
//            timeoutmsg << it
        }
        channel.right().receive {
            if (it) timeoutmsg << it
        }

        when:
        sleep(2100)

        then:
        timeoutmsg.size() == 2
        timeoutmsg[0].step == KeepAliveChannel.KEEP_ALIVE_STEP

    }

    def "keep alive pings are filtered out and not propogated left"() {

        Environment.initializeIfEmpty()
        def sched = new Scheduler()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new KeepAliveChannel(new Reactor2Dispatcher(dispatcher), "fakeproto", sched)
        def timeoutmsg = []

        channel.left().receive {
            if (it) timeoutmsg << it
        }
        channel.right().receive {
        }

        channel.right().send(
                MuonMessageBuilder.fromService("meh")
                .step(KeepAliveChannel.KEEP_ALIVE_STEP)
                .buildInbound()
        )
        channel.right().send(
                MuonMessageBuilder.fromService("meh")
                        .step(KeepAliveChannel.KEEP_ALIVE_STEP)
                        .buildInbound()
        )
        when:
        sleep(1000)

        then:
        timeoutmsg.size() == 0
    }

    def "channel receiving keep alive pings stays open"() {

        Environment.initializeIfEmpty()
        def sched = new Scheduler()
        def dispatcher = new RingBufferLocalDispatcher("channel", 32768);
        def channel = new KeepAliveChannel(new Reactor2Dispatcher(dispatcher), "fakeproto", sched)
        def timeoutmsg = []

        channel.left().receive {
            if (it) timeoutmsg << it
        }
        channel.right().receive {
        }

        when:
        15.times {
            channel.right().send(
                    MuonMessageBuilder.fromService("meh")
                            .step(KeepAliveChannel.KEEP_ALIVE_STEP)
                            .buildInbound()
            )
            sleep(1000)
        }

        then:
        timeoutmsg.size() == 0
    }
}
