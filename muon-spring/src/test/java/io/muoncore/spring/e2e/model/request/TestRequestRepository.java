package io.muoncore.spring.e2e.model.request;

import com.google.gson.JsonObject;
import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.MuonRepository;
import io.muoncore.spring.annotations.Request;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;

@MuonRepository
public interface TestRequestRepository {
    @Request("request://${muon.server.name}/getPerson")
    Person getPersonById(@Parameter("id") Long id);

    @Request("request://${muon.server.name}/addPerson")
    void addPerson(Person person);

    @Request("request://${muon.server.name}/findPerson")
    Person getPersonByNameAndAge(@Parameter("name") String name, @Parameter("age") Integer age);

    @Request("request://${muon.server.name}/findPerson")
    JsonObject getPersonByNameAndAgeString(@Parameter("name") String name, @Parameter("age") Integer age);
}
