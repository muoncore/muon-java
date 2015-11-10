package com.simplicity.services.spring.server;

import com.simplicity.services.spring.Person;
import io.muoncore.spring.annotations.MuonCommandListener;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.simplicity.services.spring.PersonDb.PERSON_DB;

@MuonController
public class Controller {

    private static final Logger LOG = Logger.getLogger(Controller.class.getCanonicalName());

    @MuonRequestListener(path = "/getPerson")
    public Person getPersonById(@Parameter("id") long id) {
        LOG.info("Got request to find PersonById[" + id + "]");
        Person person = PERSON_DB.get(id);
        LOG.info("Found person " + person);
        return person;
    }

    @MuonRequestListener(path = "/getPeople")
    //TODO Implement type propagation to get correct list elements type
    public List<Person> getPeople() {
        LOG.info("Got request to get people list");
        return new ArrayList<>(PERSON_DB.values());
    }

    @MuonRequestListener(path = "/addPerson")
    public void addPerson(Person person) {
        PERSON_DB.put(person.getId(), person);
    }
}
