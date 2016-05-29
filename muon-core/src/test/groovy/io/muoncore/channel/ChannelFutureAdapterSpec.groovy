package io.muoncore.channel

import io.muoncore.api.ChannelFutureAdapter
import io.muoncore.channel.impl.StandardAsyncChannel
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

        StandardAsyncChannel.echoOut = true
        def channel = Channels.channel("left", "right")

        channel.right().receive({

        })

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channel.left())
        def future = adapter.request("simples")

        Thread.start {
            Thread.sleep 200
            channel.right().send "wibble"
        }

        expect:
        future.get(500, TimeUnit.MILLISECONDS) == "wibble"

        cleanup:
        StandardAsyncChannel.echoOut = false
    }

    def "adapter sends shutdown to channel on timeout"() {

        Environment.initializeIfEmpty()
        StandardAsyncChannel.echoOut = true
        def channel = Channels.channel("left", "right")
        def receiver = Mock(ChannelConnection.ChannelFunction)
        channel.right().receive(receiver)

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channel.left())
        def future = adapter.request("simples")

        when:
        future.get(500, TimeUnit.MILLISECONDS)

        Thread.sleep(50)

        then:
        1 * receiver.apply(null)

        cleanup:
        StandardAsyncChannel.echoOut = false
    }

    def "adapter sends shutdown to channel on data received"() {

        Environment.initializeIfEmpty()

        StandardAsyncChannel.echoOut = true
        def channel = Channels.channel("left", "right")
        def receiver = Mock(ChannelConnection.ChannelFunction)

        channel.right().receive(receiver)

        ChannelFutureAdapter adapter = new ChannelFutureAdapter(channel.left())
        def future = adapter.request("simples")

        Thread.start {
            Thread.sleep 200
            channel.right().send "wibble"
        }

        when:
        future.get(500, TimeUnit.MILLISECONDS)

        Thread.sleep(50)

        then:
        1 * receiver.apply(null)

        cleanup:
        StandardAsyncChannel.echoOut = false
    }
}
