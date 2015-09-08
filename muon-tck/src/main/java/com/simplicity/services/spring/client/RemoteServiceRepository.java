package com.simplicity.services.spring.client;

import com.simplicity.services.spring.Person;
import io.muoncore.spring.annotations.Command;
import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.annotations.Query;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.List;

@MuonRepository
public interface RemoteServiceRepository {
    @Query("muon://${muon.server.name}/getPerson")
    Person getPersonById(@Parameter("id") Long id);

    @Command("muon://${muon.server.name}/addPerson")
    void addPerson(Person person);

    @Query("muon://${muon.server.name}/getPeople")
    List<Person> getPeople();
}
