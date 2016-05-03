package com.simplicity.services.spring.server;

import com.simplicity.services.spring.PersonRecord;
import io.muoncore.spring.annotations.MuonController;
import io.muoncore.spring.annotations.MuonRequestListener;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.simplicity.services.spring.PersonDb.PERSON_DB;

@MuonController
public class Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class.getCanonicalName());

    @MuonRequestListener(path = "/getPerson")
    public PersonRecord getPersonById(@Parameter("id") long id) {
        LOG.info("Got request to find PersonById[" + id + "]");
        PersonRecord person = PERSON_DB.get(id);
        LOG.info("Found person " + person);
        return person;
    }

    @MuonRequestListener(path = "/getPeople")
    //TODO Implement type propagation to get correct list elements type
    public List<PersonRecord> getPeople() {
        LOG.info("Got request to get people list");
        return new ArrayList<>(PERSON_DB.values());
    }

    @MuonRequestListener(path = "/addPerson")
    public void addPerson(PersonRecord person) {
        PERSON_DB.put(person.getId(), person);
    }
}
