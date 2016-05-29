package io.muoncore.channel.support

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static java.util.concurrent.TimeUnit.MILLISECONDS

class SchedulerSpec extends Specification {

    def "executes stuff on a timer"(){
        def s = new Scheduler()

        def d

        when:
        s.executeIn(500, MILLISECONDS) {
            d = "hello"
        }

        then:
        new PollingConditions(timeout: 5).eventually {
            d != null
        }
    }

    def "setting a timer and cancelling causes the trigger to not be executed"() {
        def s = new Scheduler()

        def d

        when:
        def ret = s.executeIn(500, MILLISECONDS) {
            d = "hello"
        }

        and:
        ret.cancel()

        sleep(600)

        then:
        d == null
    }
}
