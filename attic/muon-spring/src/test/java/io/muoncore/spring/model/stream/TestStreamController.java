package io.muoncore.spring.model.stream;

import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonStreamListener;

import java.util.List;

@MuonController
public class TestStreamController {

    private TestStreamController delegatingMock;

    @MuonStreamListener(url = "stream://${muon.streamSource.name}/personStream")
    public void addPersonEvent(Person person) {
        delegatingMock.addPersonEvent(person);
    }

    @MuonStreamListener(url = "stream://${muon.streamSource.name}/manyPeopleStream")
    public void manyPeopleEvent(List<Person> people) {
        delegatingMock.manyPeopleEvent(people);
    }

    @MuonStreamListener(url = "stream://${muon.streamSource.name}/removePersonStream")
    public void removePersonEvent(Person person) {
        delegatingMock.removePersonEvent(person);
    }

    public void setDelegatingMock(TestStreamController delegatingMock) {
        this.delegatingMock = delegatingMock;
    }
}
