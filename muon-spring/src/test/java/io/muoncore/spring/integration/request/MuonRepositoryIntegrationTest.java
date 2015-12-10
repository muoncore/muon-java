package io.muoncore.spring.integration.request;

import io.muoncore.Muon;
import io.muoncore.future.ImmediateReturnFuture;
import io.muoncore.future.MuonFuture;
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.EnableMuonRepositories;
import io.muoncore.spring.integration.MockedMuonConfiguration;
import io.muoncore.spring.model.request.TestRequestRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MuonRepositoryIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@EnableMuonRepositories(basePackages = "io.muoncore.spring.model.request")
@Import(MockedMuonConfiguration.class)
public class MuonRepositoryIntegrationTest {

    public static final Person PETER = new Person(123L, "Peter", 23);
    public static final Person MIKE = new Person(234L, "Mike", 30);

    @Autowired
    private Muon muon;

    @Autowired
    private TestRequestRepository testRequestRepository;

    private ArgumentCaptor<String> urlCaptor;
    private ArgumentCaptor<Object> payloadCaptor;
    private ArgumentCaptor<Type> classCaptor;

    @Before
    public void setUp() throws Exception {
        reset(muon);
        urlCaptor = ArgumentCaptor.forClass(String.class);
        payloadCaptor = ArgumentCaptor.forClass(Object.class);
        classCaptor = ArgumentCaptor.forClass(Type.class);
    }

    private <T> MuonFuture<Response<T>> createReturnFuture(T data) throws Exception {
        return new ImmediateReturnFuture<>(new Response<>(200, data));
    }

    @Test
    public void processesSimpleRequest() throws Exception {
        final MuonFuture result = createReturnFuture(PETER);
        when(muon.request(urlCaptor.capture(), payloadCaptor.capture(), classCaptor.capture())).thenReturn(result);

        Person person = testRequestRepository.getPersonById(PETER.getId());
        assertThat(person, is(PETER));

        Map payload = (Map) payloadCaptor.getValue();
        assertThat(urlCaptor.getValue(), is("request://muon-server/getPerson"));
        assertThat(payload.get("id"), is(PETER.getId()));
        assertThat(classCaptor.getValue(), equalTo(Person.class));
    }

    @Test
    public void processesMultipleParametersRequest() throws Exception {
        final MuonFuture result = createReturnFuture(PETER);
        when(muon.request(urlCaptor.capture(), payloadCaptor.capture(), classCaptor.capture())).thenReturn(result);

        Person person = testRequestRepository.getPersonByNameAndAge(PETER.getName(), PETER.getAge());
        assertThat(person, is(PETER));

        Map payload = (Map) payloadCaptor.getValue();
        assertThat(urlCaptor.getValue(), is("request://muon-server/findPerson"));
        assertThat(payload.get("name"), is(PETER.getName()));
        assertThat(payload.get("age"), is(PETER.getAge()));
        assertThat(classCaptor.getValue(), equalTo(Person.class));
    }

    @Test
    public void processesListQueryWithVoidArguments() throws Exception {

        List<Person> expectedResult = Arrays.asList(PETER, MIKE);

        when(muon.request(urlCaptor.capture(), payloadCaptor.capture(), classCaptor.capture())).thenReturn(createReturnFuture(expectedResult));

        List<Person> result = testRequestRepository.getPeople();

        assertThat(result.size(), is(2));
        assertThat(result.get(0), is(PETER));
        assertThat(result.get(1), is(MIKE));

        assertThat(urlCaptor.getValue(), is("request://muon-server/getPeople"));
        final ParameterizedType type = (ParameterizedType) classCaptor.getValue();
        assertThat(type.getRawType(), equalTo(List.class));
        assertThat(type.getActualTypeArguments(), equalTo(new Class[]{Person.class}));


        assertThat(payloadCaptor.getValue(), nullValue());
    }
}
