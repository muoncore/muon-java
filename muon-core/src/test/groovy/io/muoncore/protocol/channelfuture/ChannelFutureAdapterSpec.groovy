package io.muoncore.protocol.channelfuture

import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.ChannelFutureAdapter
import io.muoncore.channel.async.StandardAsyncChannelConnection
import spock.lang.Specification

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ChannelFutureAdapterSpec extends Specification {

    def "adapter request sends the message down the channel"() {

        def channelConnection = Mock(ChannelConnection)

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channelConnection)

        when:
        adapter.request("simples")

        then:
        1 * channelConnection.send("simples")
    }

    def "adapter calls channel receive"() {

        def func

        def channelConnection = Mock(ChannelConnection) {
            1 * receive({
                func = it
            } as ChannelConnection.ChannelFunction)
        }

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channelConnection)
        adapter.request("simples")

        expect:
        func != null
    }

    def "adapter returns a future that the receive function can cause to return"() {
        LinkedBlockingQueue queue = new LinkedBlockingQueue()
        LinkedBlockingQueue queue2 = new LinkedBlockingQueue()

        StandardAsyncChannelConnection con = new StandardAsyncChannelConnection(
                queue, queue2
        );

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(con)
        def future = adapter.request("simples")

        Thread.start {
            Thread.sleep 200
            queue << "wibble"
        }

        expect:
        future.get(500, TimeUnit.MILLISECONDS) == "wibble"
    }
}
