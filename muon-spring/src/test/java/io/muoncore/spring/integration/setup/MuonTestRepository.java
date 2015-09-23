package io.muoncore.spring.integration.setup;

import io.muoncore.spring.annotations.Command;
import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.annotations.Query;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.List;

@MuonRepository
public interface MuonTestRepository {
    @Query("muon://${muon.service.name}/getPerson")
    Person getPersonById(@Parameter("id") Long id);

    @Command("muon://${muon.service.name}/addPerson")
    void addPerson(Person person);

    @Query("muon://${muon.service.name}/getPeople")
    List<Person> getPeople();
}
