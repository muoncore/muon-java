package com.simplicity.services.spring.client;

import com.simplicity.services.spring.Person;
import io.muoncore.spring.annotations.Command;
import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.annotations.Query;
import io.muoncore.spring.annotations.Request;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.List;

@MuonRepository
public interface RemoteServiceRepository {
    @Request("request://${muon.server.name}/getPerson")
    Person getPersonById(@Parameter("id") Long id);

    @Request("request://${muon.server.name}/addPerson")
    void addPerson(Person person);

    @Request("request://${muon.server.name}/getPeople")
    List<Person> getPeople();
}
