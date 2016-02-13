package com.simplicity.services.spring.client;

import com.simplicity.services.spring.PersonRecord;
import io.muoncore.spring.annotations.EnableMuon;
import io.muoncore.spring.annotations.EnableMuonRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.List;

import static com.simplicity.services.spring.PersonBuilder.aDefaultPerson;

@Configuration
@EnableMuon(serviceName = "${muon.client.name}",
        tags = {"${muon.client.tag1}", "${muon.client.tag2}"})
@EnableMuonRepositories(basePackages = {"com.simplicity.services.spring.client"})
@PropertySource("classpath:application.properties")
public class ClientApplication {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Autowired
    private RemoteServiceRepository remoteServiceRepository;

    public static void main(String[] args) {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(ClientApplication.class);

        ctx.getBean(ClientApplication.class).run();
    }

    public void run() {
        remoteServiceRepository.addPerson(aDefaultPerson().withId(100L).build());

        final PersonRecord personById = remoteServiceRepository.getPersonById(100L);
        System.out.println(personById);

        List<PersonRecord> list = remoteServiceRepository.getPeople();
        System.out.println(list);

    }
}
