package io.muoncore.spring.e2e;

import com.google.gson.JsonObject;
import io.muoncore.spring.Person;
import io.muoncore.spring.e2e.request.ClientServiceConfiguration;
import io.muoncore.spring.e2e.request.ServerServiceConfiguration;
import io.muoncore.spring.model.request.TestRequestController;
import io.muoncore.spring.model.request.TestRequestRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static io.muoncore.spring.PersonBuilder.aDefaultPerson;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class RequestResponseE2ETest {

    private static TestRequestRepository testRequestRepository;

    private static TestRequestController testControllerDelegatingMock;

    @BeforeClass
    public static void setUpContext() throws Exception {
        testControllerDelegatingMock = Mockito.mock(TestRequestController.class);
        ApplicationContext clientServiceContext = new AnnotationConfigApplicationContext(ClientServiceConfiguration.class);
        ApplicationContext serverServiceContext = new AnnotationConfigApplicationContext(ServerServiceConfiguration.class);

        testRequestRepository = clientServiceContext.getBean(TestRequestRepository.class);
        serverServiceContext
                .getBean(TestRequestController.class)
                .setDelegatingMock(testControllerDelegatingMock);
    }

    @Before
    public void setUp() throws Exception {
        Mockito.reset(testControllerDelegatingMock);
    }

    @Test
    public void performsSimpleRequest() throws Exception {
        final Person person = aDefaultPerson().build();
        when(testControllerDelegatingMock.getPerson(4L)).thenReturn(person);

        assertThat(testRequestRepository.getPersonById(4L), equalTo(person));
        verify(testControllerDelegatingMock, times(1)).getPerson(4L);

        verifyNoMoreInteractions(testControllerDelegatingMock);

    }

    @Test
    public void performsObjectRequest() throws Exception {
        final Person person = aDefaultPerson().withId(12223L).build();

        testRequestRepository.addPerson(person);
        verify(testControllerDelegatingMock, times(1)).addPerson(person);
        verifyNoMoreInteractions(testControllerDelegatingMock);

    }

    @Test
    public void performsMultipleParameterRequest() throws Exception {
        final Person expectedPerson = aDefaultPerson().build();
        when(testControllerDelegatingMock.findPerson("Thomas", 43)).thenReturn(expectedPerson);

        final Person thomas = testRequestRepository.getPersonByNameAndAge("Thomas", 43);

        assertThat(thomas, equalTo(expectedPerson));
        verify(testControllerDelegatingMock, times(1)).findPerson("Thomas", 43);

        verifyNoMoreInteractions(testControllerDelegatingMock);
    }

    @Test
    public void performsRequestToString() throws Exception {
        final Person expectedPerson = aDefaultPerson().build();
        when(testControllerDelegatingMock.findPerson("Thomas", 43)).thenReturn(expectedPerson);

        final JsonObject thomas = testRequestRepository.getPersonByNameAndAgeString("Thomas", 43);
        assertThat(thomas.toString(), equalTo("{\"id\":100,\"name\":\"Adam Smith\",\"age\":43}"));
        verify(testControllerDelegatingMock, times(1)).findPerson("Thomas", 43);

        verifyNoMoreInteractions(testControllerDelegatingMock);
    }

    @Test
    public void performsRequestOfListOfObjects() throws Exception {
        final List<Person> personList = new ArrayList<>();
        personList.add(aDefaultPerson().withName("Thomas").build());
        personList.add(aDefaultPerson().withName("Markus").build());
        when(testControllerDelegatingMock.getPeople()).thenReturn(personList);

        List<Person> people = testRequestRepository.getPeople();

        verify(testControllerDelegatingMock, timeout(100).only()).getPeople();

        assertThat(people, equalTo(personList));
    }

    @Test
    public void passesListOfObjectsAsParam() throws Exception {
        final List<Person> people = new ArrayList<>();
        people.add(aDefaultPerson().withName("Thomas").build());
        people.add(aDefaultPerson().withName("Markus").build());

        testRequestRepository.replacePeople(people);

        verify(testControllerDelegatingMock, timeout(100).only()).replacePeople(people);
    }

}
