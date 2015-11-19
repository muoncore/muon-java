package io.muoncore.protocol.channelfuture
import io.muoncore.channel.ChannelConnection
import io.muoncore.channel.ChannelFutureAdapter
import io.muoncore.channel.async.StandardAsyncChannel
import reactor.Environment
import spock.lang.Specification

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
        Environment.initializeIfEmpty()

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

        def channel = new StandardAsyncChannel()

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channel.left())
        def future = adapter.request("simples")

        Thread.start {
            Thread.sleep 200
            channel.right().send "wibble"
        }

        expect:
        future.get(500, TimeUnit.MILLISECONDS) == "wibble"
    }
}
