package io.muoncore.protocol.requestresponse

import io.muoncore.protocol.requestresponse.server.DynamicRequestResponseHandlers
import io.muoncore.protocol.requestresponse.server.RequestResponseServerHandler
import io.muoncore.protocol.requestresponse.server.RequestWrapper
import spock.lang.Specification

import java.util.function.Predicate

class DynamicRequestResponseHandlersSpec extends Specification {

    def "registry accepts handlers and choose the correct one based on predicate" () {


        given:
        def registry = new DynamicRequestResponseHandlers(new StubRequestResponseServerHandler(id:8, predicate: {false}))
        registry.addHandler(new StubRequestResponseServerHandler(id:1, predicate: {
            false
        }))
        registry.addHandler(new StubRequestResponseServerHandler(id:2, predicate: {
            false
        }))
        registry.addHandler(new StubRequestResponseServerHandler(id:3, predicate: {
            true
        }))
        registry.addHandler(new StubRequestResponseServerHandler(id:4, predicate: {
            false
        }))

        expect:
        registry.findHandler(new Request(id:"simples")).id == 3

    }

    def "registry accepts handlers and gives the default if none found" () {

        given:
        def registry = new DynamicRequestResponseHandlers(new StubRequestResponseServerHandler(id:8, predicate: {false}))
        registry.addHandler(new StubRequestResponseServerHandler(id:1, predicate: {
            false
        }))
        registry.addHandler(new StubRequestResponseServerHandler(id:2, predicate: {
            false
        }))

        expect:
        registry.findHandler(new Request(id:"simples")).id == 8

    }
}

class StubRequestResponseServerHandler implements RequestResponseServerHandler {
    int id
    Predicate<Request> predicate

    @Override
    void handle(RequestWrapper request) {
        throw new IllegalStateException("Not implemented in stub!")
    }
}
