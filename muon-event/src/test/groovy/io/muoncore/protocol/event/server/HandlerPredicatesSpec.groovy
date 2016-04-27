package io.muoncore.protocol.event.server

import io.muoncore.protocol.requestresponse.server.HandlerPredicates
import io.muoncore.protocol.requestresponse.server.ServerRequest
import spock.lang.Specification

class HandlerPredicatesSpec extends Specification {

    def "path predicate matches against url path"() {

        def predicate = HandlerPredicates.path("/hello")

        expect:
        predicate.matcher().test(new ServerRequest(new URI("request://someservice/hello"), [] as byte[], null, null))

    }

}
