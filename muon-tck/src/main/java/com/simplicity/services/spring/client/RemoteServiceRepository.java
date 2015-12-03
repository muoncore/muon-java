package com.simplicity.services.spring.client;

import com.simplicity.services.spring.PersonRecord;
import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.annotations.Request;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

import java.util.List;

@MuonRepository
public interface RemoteServiceRepository {
    @Request("request://${muon.server.name}/getPerson")
    PersonRecord getPersonById(@Parameter("id") Long id);

    @Request("request://${muon.server.name}/addPerson")
    void addPerson(PersonRecord person);

    @Request("request://${muon.server.name}/getPeople")
    List<PersonRecord> getPeople();
}
