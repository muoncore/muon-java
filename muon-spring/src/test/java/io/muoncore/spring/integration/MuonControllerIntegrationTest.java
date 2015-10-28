package io.muoncore.spring.integration;

import io.muoncore.Muon;
import io.muoncore.MuonService;
import io.muoncore.future.MuonFuture;
import io.muoncore.spring.annotations.EnableMuonControllers;
import io.muoncore.spring.integration.setup.MuonTestController;
import io.muoncore.spring.integration.setup.Person;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import reactor.rx.broadcast.Broadcaster;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MuonControllerIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@EnableMuonControllers(streamKeepAliveTimeout = 500)
@Import(MockedMuonConfiguration.class)
@ComponentScan(basePackages = "io.muoncore.spring.integration.setup")
public class MuonControllerIntegrationTest {

    public static final Person PETER = new Person(123l, "Peter", 23);
    public static final Person MIKE = new Person(234l, "Mike", 30);
    public static final String QUERY_EXPECTED_PERSON_NAME = "personName";
    @Autowired
    private Muon muon;
    @Autowired
    private MuonTestController testController;

    private ArgumentCaptor<String> resourceNameCaptor;
    private ArgumentCaptor<Class> typeCaptor;
    private ArgumentCaptor<MuonService.MuonQuery> muonQueryListenerCaptor;
    private ArgumentCaptor<MuonService.MuonCommand> muonCommandListenerCaptor;
    private ArgumentCaptor<Broadcaster> streamBroadcaster;

    @Before
    public void setUp() throws Exception {
        resourceNameCaptor = ArgumentCaptor.forClass(String.class);
        typeCaptor = ArgumentCaptor.forClass(Class.class);
        muonQueryListenerCaptor = ArgumentCaptor.forClass(MuonService.MuonQuery.class);
        muonCommandListenerCaptor = ArgumentCaptor.forClass(MuonService.MuonCommand.class);
        streamBroadcaster = ArgumentCaptor.forClass(Broadcaster.class);
    }

    @Test
    public void processesMuonQueries() throws ExecutionException, InterruptedException {

        verifyMuonQuerySetupProcess();
        assertThat(resourceNameCaptor.getValue(), is("/queryName"));
        assertThat(typeCaptor.getValue(), equalTo(Object.class));

        testController.setExpectedPersonName(QUERY_EXPECTED_PERSON_NAME);
        testController.setResponsePerson(PETER);


        MuonFuture<Person> personFuture = muonQueryListenerCaptor.getValue().onQuery(sampleQueryEvent());

        assertThat(personFuture.get(), is(PETER));
    }

    private void verifyMuonQuerySetupProcess() {
        verify(muon, times(1)).onQuery(resourceNameCaptor.capture(), typeCaptor.capture(), muonQueryListenerCaptor.capture());
    }

    @Test
    public void processesMuonCommands() throws ExecutionException, InterruptedException {
        verifyMuonCommandSetupProcess();
        assertThat(resourceNameCaptor.getValue(), is("/commandName"));
        assertThat(typeCaptor.getValue(), equalTo(Person.class));

        muonCommandListenerCaptor.getValue().onCommand(sampleCommandEvent());

        assertThat(testController.getAddedPerson(), is(MIKE));
    }

    private void verifyMuonCommandSetupProcess() {
        verify(muon, times(1)).onCommand(resourceNameCaptor.capture(), typeCaptor.capture(), muonCommandListenerCaptor.capture());
    }

    @Test
    public void processesMuonStreams() throws URISyntaxException {
        verifyMuonStreamSetupProcess(1);
        assertThat(resourceNameCaptor.getValue(), is("muon://streamService/stream"));
        assertThat(typeCaptor.getValue(), equalTo(Person.class));

        streamBroadcaster.getValue().onNext(MIKE);

        assertThat(testController.getReceivedStreamEvent(), is(MIKE));
    }

    private void verifyMuonStreamSetupProcess(int streamSubscriptions) throws URISyntaxException {
        verify(muon, times(streamSubscriptions)).subscribe(resourceNameCaptor.capture(), typeCaptor.capture(), streamBroadcaster.capture());
    }

    @Test
    public void reconnectsStreamOnError() throws URISyntaxException, InterruptedException {
        verifyMuonStreamSetupProcess(1);
        streamBroadcaster.getValue().onError(new Exception());

        Thread.sleep(1000);

        verifyMuonStreamSetupProcess(2);
        assertThat(resourceNameCaptor.getValue(), is("muon://streamService/stream"));
        assertThat(typeCaptor.getValue(), equalTo(Person.class));

    }

    private MuonResourceEvent<Map> sampleQueryEvent() {
        MuonResourceEvent<Map> queryEvent = new MuonResourceEvent<Map>(URI.create("muon://sample-service/point"));
        queryEvent.setDecodedContent(sampleParameterMap());
        return queryEvent;
    }

    private MuonResourceEvent<Person> sampleCommandEvent() {
        MuonResourceEvent<Person> queryEvent = new MuonResourceEvent<Person>(URI.create("muon://sample-service/point"));
        queryEvent.setDecodedContent(MIKE);
        return queryEvent;
    }

    private HashMap<Object, Object> sampleParameterMap() {
        HashMap<Object, Object> sampleParametersMap = new HashMap<>();
        sampleParametersMap.put("personName", QUERY_EXPECTED_PERSON_NAME);
        return sampleParametersMap;
    }

}
