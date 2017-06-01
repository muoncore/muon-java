package io.muoncore.protocol.rpc

import io.muoncore.codec.Codecs
import io.muoncore.protocol.rpc.server.HandlerPredicates
import io.muoncore.protocol.rpc.server.RequestResponseHandlers
import io.muoncore.protocol.rpc.server.RequestResponseServerHandlerApi
import spock.lang.Specification

class RequestResponseServerHandlerApiSpec extends Specification {

    def "handleRequest API creates a new RequestResponseHandler with the passed in artifacts"() {
        def handler = Mock(RequestResponseServerHandlerApi.Handler)
        def requestResponseHandlers = Mock(RequestResponseHandlers)
        def codecs = Mock(Codecs)

        def requestResponseServerHandlerApi = new RequestResponseServerHandlerApi() {
            @Override
            RequestResponseHandlers getRequestResponseHandlers() {
                return requestResponseHandlers
            }

          @Override
          Codecs getCodecs() {
            return codecs
          }
        }

        when:
        requestResponseServerHandlerApi.handleRequest(HandlerPredicates.all(), handler)

        then:
        1 * requestResponseHandlers.addHandler(_)
    }
}
