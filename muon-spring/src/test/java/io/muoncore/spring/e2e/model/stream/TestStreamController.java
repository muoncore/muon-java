package io.muoncore.spring.e2e.model.stream;

import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonStreamListener;

/**
 * Created by volod on 11/24/2015.
 */
@MuonController
public class TestStreamController {

    private TestStreamController delegatingMock;

    @MuonStreamListener(url = "stream://${muon.streamSource.name}/personStream")
    public void addPersonEvent(Person person) {
        delegatingMock.addPersonEvent(person);
    }

    @MuonStreamListener(url = "stream://${muon.streamSource.name}/removePersonStream")
    public void removePersonEvent(Person person) {
        delegatingMock.removePersonEvent(person);
    }

    public void setDelegatingMock(TestStreamController delegatingMock) {
        this.delegatingMock = delegatingMock;
    }
}
