package io.muoncore.spring.integration;

import io.muoncore.spring.annotations.EnableMuonRepositories;
import io.muoncore.spring.Person;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MuonRepositoryIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@EnableMuonRepositories(basePackages = "io.muoncore.spring.integration.setup")
@Import(MockedMuonConfiguration.class)
public class MuonRepositoryIntegrationTest {

    public static final Person PETER = new Person(123L, "Peter", 23);
    public static final Person MIKE = new Person(234L, "Mike", 30);

/*
    @Autowired
    private Muon muon;

    @Autowired
    private MuonTestRepository muonTestRepository;

    private ArgumentCaptor<String> stringCaptor;
    private ArgumentCaptor<MuonResourceEvent> eventCaptor;
    private ArgumentCaptor<Class> classCaptor;

    @Before
    public void setUp() throws Exception {
        reset(muon);
        stringCaptor = ArgumentCaptor.forClass(String.class);
        eventCaptor = ArgumentCaptor.forClass(MuonResourceEvent.class);
        classCaptor = ArgumentCaptor.forClass(Class.class);
    }

    private ImmediateReturnFuture createReturnFuture(Object data) throws Exception {
        final MuonClient.MuonResult<Object> muonResult = new MuonClient.MuonResult<>();
        final MuonResourceEvent event = new MuonResourceEvent(new URI("does-not-matter-url"));
        event.setDecodedContent(data);
        muonResult.setEvent(event);
        return new ImmediateReturnFuture<>(muonResult);
    }

    @Test
    public void processesSimpleQuery() throws Exception {
        final ImmediateReturnFuture result = createReturnFuture(PETER);
        when(muon.query(eventCaptor.capture(), any())).thenReturn(result, (ImmediateReturnFuture) null);

        Person person = muonTestRepository.findPerson(PETER.getId());
        assertThat(person, is(PETER));

        final MuonResourceEvent<Person> value = eventCaptor.getValue();
        assertThat(value.getUri().toString(), is("muon://muon-test/getPerson"));
        assertThat(((Map)value.getDecodedContent()).get("id"), is(PETER.getId()));
        assertThat(value.getHeaders().size(), is(0));
    }

    @Test
    public void processesSimpleCommand() throws Exception {
        when(muon.command(anyString(), any(MuonResourceEvent.class), any(Class.class))).thenReturn(createReturnFuture("test"));

        muonTestRepository.addPerson(MIKE);

        verify(muon, times(1)).command(stringCaptor.capture(), eventCaptor.capture(), classCaptor.capture());

        assertThat(stringCaptor.getValue(), is("muon://muon-test/addPerson"));
        assertThat(classCaptor.getValue(), equalTo(void.class));

        final MuonResourceEvent muonResourceEvent = eventCaptor.getValue();
        assertThat(muonResourceEvent.getUri().toString(), is("muon://muon-test/addPerson"));
        assertThat(muonResourceEvent.getDecodedContent(), is(MIKE));
    }

    @Test
    public void processesListQueryWithVoidArguments() throws Exception {

        List<Person> expectedResult = Arrays.asList(PETER, MIKE);
        when(muon.query(eventCaptor.capture(), classCaptor.capture())).thenReturn(createReturnFuture(expectedResult));

        List<Person> result = muonTestRepository.getPeople();

        verify(muon, times(1)).query(any(MuonResourceEvent.class), any(Class.class));

        assertThat(result.size(), is(2));
        assertThat(result.get(0), is(PETER));
        assertThat(result.get(1), is(MIKE));

        assertThat(classCaptor.getValue(), equalTo(List.class));
        assertThat(eventCaptor.getValue().getUri().toString(), is("muon://muon-test/getPeople"));
        assertThat(eventCaptor.getValue().getDecodedContent(), nullValue());
    }
*/
}
