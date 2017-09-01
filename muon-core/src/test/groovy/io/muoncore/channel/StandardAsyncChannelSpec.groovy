package io.muoncore.channel
import io.muoncore.channel.impl.StandardAsyncChannel
import reactor.Environment
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class StandardAsyncChannelSpec extends Specification {

//    def "StandardAsyncChannelConnection forwards to listening function"() {
//        def data
//        def func = { data = it }
//
//        LinkedBlockingQueue queue = new LinkedBlockingQueue()
//        LinkedBlockingQueue queue2 = new LinkedBlockingQueue()
//
//        StandardAsyncChannelConnection con = new StandardAsyncChannelConnection(
//                queue, queue2
//        );
//        con.receive(func)
//
//        when:
//        queue << "wibble"
//
//        then:
//        new PollingConditions(timeout: 5).eventually {
//            data == "wibble"
//        }
//    }
//
//    def "StandardAsyncChannelConnection.send posts onto the correct queue"() {
//
//        LinkedBlockingQueue queue = new LinkedBlockingQueue()
//        LinkedBlockingQueue queue2 = new LinkedBlockingQueue()
//
//        StandardAsyncChannelConnection con = new StandardAsyncChannelConnection(
//                queue, queue2
//        );
//
//        when:
//        con.send("simples")
//
//        then:
//        new PollingConditions(timeout: 5).eventually {
//            queue2.poll() == "simples"
//        }
//
//    }

    def "StandardAsyncChannel sets up bidirectional comms between the two interfaces"() {

        def dataleft
        def dataright

        Environment.initializeIfEmpty();

        def asyncChannel = new StandardAsyncChannel("leftbit", "rightbit", new Reactor2Dispatcher(Environment.sharedDispatcher()))
        asyncChannel.left().receive {
            dataleft = it
        }
        asyncChannel.right().receive {
            dataright = it
        }

        when:
        asyncChannel.left().send("simples")
        asyncChannel.right().send("simpleton")

        then:
        new PollingConditions().eventually {
            dataleft == "simpleton"
            dataright == "simples"
        }
    }
}
