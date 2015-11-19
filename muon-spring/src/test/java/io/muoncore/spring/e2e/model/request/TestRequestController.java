package io.muoncore.spring.e2e.model.request;

import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.logging.Logger;

@MuonController
public class TestRequestController {
    private static final Logger LOG = Logger.getLogger(TestRequestController.class.getCanonicalName());

    public TestRequestController delegatingMock;

    @MuonRequestListener(path = "/getPerson")
    public Person getPerson(@Parameter("id") long id) {
        return delegatingMock.getPerson(id);
    }

    @MuonRequestListener(path = "/findPerson")
    public Person findPerson(@Parameter("name") String name, @Parameter("age") Integer age) {
        return delegatingMock.findPerson(name, age);
    }

    @MuonRequestListener(path = "/addPerson")
    public void addPerson(Person person) {
        delegatingMock.addPerson(person);
    }

    public void setDelegatingMock(TestRequestController delegatingMock) {
        this.delegatingMock = delegatingMock;
    }
}
