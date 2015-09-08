package com.simplicity.services.spring.server;

import io.muoncore.spring.annotations.EnableMuon;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = {"com.simplicity.services.spring.server"})
@EnableMuon(serviceName = "${muon.server.name}", tags = {"${muon.server.tag1}", "${muon.server.tag2}"}, discoveryUrl = "${muon.discoveryUrl}")
@PropertySource("classpath:application.properties")
public class ServerApplication {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public static void main(String[] args) {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(ServerApplication.class);
    }
}
