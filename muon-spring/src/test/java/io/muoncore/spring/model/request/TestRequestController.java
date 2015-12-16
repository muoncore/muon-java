package io.muoncore.spring.model.request;

import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.List;

@MuonController
public class TestRequestController {

    public TestRequestController delegatingMock;

    @MuonRequestListener(path = "/getPerson")
    public Person getPerson(@Parameter("id") long id) {
        return delegatingMock.getPerson(id);
    }

    @MuonRequestListener(path = "/findPerson")
    public Person findPerson(@Parameter("name") String name, @Parameter("age") Integer age) {
        return delegatingMock.findPerson(name, age);
    }

    @MuonRequestListener(path = "/getPeople")
    public List<Person> getPeople() {
        return delegatingMock.getPeople();
    }

    @MuonRequestListener(path = "/addPerson")
    public void addPerson(Person person) {
        delegatingMock.addPerson(person);
    }

    public void setDelegatingMock(TestRequestController delegatingMock) {
        this.delegatingMock = delegatingMock;
    }

    @MuonRequestListener(path = "/replacePeople")
    public void replacePeople(List<Person> people) {
        delegatingMock.replacePeople(people);
    }
}
