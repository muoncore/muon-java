package io.muoncore.protocol.reactivestream.server

import io.muoncore.codec.Codecs
import io.muoncore.config.AutoConfiguration
import org.reactivestreams.Publisher
import spock.lang.Specification

class ReactiveStreamServerStackSpec extends Specification {

    def "protocol descriptor is correct"() {

        def pub1 = Mock(Publisher)

        def lookup = Mock(PublisherLookup) {
            getPublishers() >> [
                    new PublisherLookup.PublisherRecord("simple", PublisherLookup.PublisherType.HOT, pub1),
                    new PublisherLookup.PublisherRecord("tombola", PublisherLookup.PublisherType.COLD, pub1)
            ]
        }
        def codecs = Mock(Codecs)
        def config = new AutoConfiguration()

        def stack = new ReactiveStreamServerStack(lookup, codecs, config)

        expect:
        stack.protocolDescriptor.protocolScheme == "reactive-stream"
        stack.protocolDescriptor.operations.size() == 2
    }
}
