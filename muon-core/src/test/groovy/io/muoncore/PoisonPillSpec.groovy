package io.muoncore

import spock.lang.Specification

class PoisonPillSpec extends Specification {

    def "poison pill needs adding in"() {
        expect:
        throw new IllegalStateException("""
Poison Pill is a message that will be dropped down the channels to terminate them.

All protocols should use this to close up a channel and clean up resources.
""")
    }
}
