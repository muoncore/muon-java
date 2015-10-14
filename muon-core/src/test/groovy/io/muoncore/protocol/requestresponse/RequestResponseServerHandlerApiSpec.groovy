package io.muoncore.protocol.requestresponse

import io.muoncore.protocol.requestresponse.server.RequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandlerApi
import spock.lang.Specification

import java.util.function.Predicate

class RequestResponseServerHandlerApiSpec extends Specification {

    def "handleRequest API creates a new RequestResponseHandler with the passed in artifacts"() {
        def handler = Mock(RequestResponseServerHandlerApi.Handler)
        def predicate = Mock(Predicate)
        def requestResponseHandlers = Mock(RequestResponseHandlers)

        def requestResponseServerHandlerApi = new RequestResponseServerHandlerApi() {
            @Override
            RequestResponseHandlers getRequestResponseHandlers() {
                return requestResponseHandlers
            }
        }

        when:
        requestResponseServerHandlerApi.handleRequest(predicate, handler)

        then:
        1 * requestResponseHandlers.addHandler(_)
    }
}
