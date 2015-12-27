package io.muoncore.testsupport
import com.google.common.eventbus.EventBus
import spock.lang.Specification

class EventChainSpec extends Specification {

    def discovery = new EventBus()

   /* def "given UserCreated, a notification should be sent"() {

        def es = new EventStoreInMem(discovery)

        //set up the data fixture
        def userEventId = userCreated()

        when:
        // start up the local service
        def svc = serviceUnderTest()


        then:
        new PollingConditions().eventually {
            EventNode userNode = es.loadEventGraphFrom(userEventId)

            userNode.children.size() == 1
            userNode.children[0].type == "NotificationSent"
        }
    }

    def "given UserCreated, and existing notification, no notification"() {

        def es = new EventStoreInMem(discovery)

        //set up the data fixture
        def userEventId = userCreated()
        notificationSent(userEventId)

        when:
        // start up the local service
        def svc = serviceUnderTest()


        then:
        new PollingConditions().eventually {
            EventNode2 userNode = es.loadEventGraphFrom(userEventId)
            userNode.children.size() == 1
        }
    }

    def serviceUnderTest() {

        //muon with stub email service
    }

    def autoId() {
        UUID.randomUUID().toString()
    }

    def userCreated() {
        es.event(new Event(
                "UserCreated",
                autoId(),
                "",
                "userService",
                [
                userName: "simpleperson",
                first   : "david",
                last    : "dawson"
        ]))
    }

    def notificationSent() {
        es.event(new Event(
                "UserCreated",
                autoId(),
                "",
                "userService",
                [
                        userName: "simpleperson",
                        first   : "david",
                        last    : "dawson"
                ]))
    }*/
}
