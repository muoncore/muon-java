package io.muoncore.spring.integration.setup;

import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonCommandListener;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.MuonStreamListener;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

@MuonController
public class MuonTestController {

    private String expectedPersonName;
    private Person responsePerson;
    private Person addedPerson;
    private Person receivedStreamEvent;

    @MuonRequestListener(path = "/queryName")
    public Person getPerson(@Parameter("personName") String personName) {
        if (expectedPersonName.equals(personName)) {
            return responsePerson;
        }
        else {
            return null;
        }
    }

    @MuonCommandListener(path = "/commandName")
    public void addPerson(Person person) {
        addedPerson = person;
    }

    @MuonStreamListener(url = "muon://streamService/stream")
    public void streamUpdate(Person personStreamEvent) {
        receivedStreamEvent = personStreamEvent;
    }

    public Person getResponsePerson() {
        return responsePerson;
    }

    public void setResponsePerson(Person responsePerson) {
        this.responsePerson = responsePerson;
    }

    public String getExpectedPersonName() {
        return expectedPersonName;
    }

    public void setExpectedPersonName(String expectedPersonName) {
        this.expectedPersonName = expectedPersonName;
    }

    public Person getAddedPerson() {
        return addedPerson;
    }

    public void setAddedPerson(Person addedPerson) {
        this.addedPerson = addedPerson;
    }

    public Person getReceivedStreamEvent() {
        return receivedStreamEvent;
    }
}
