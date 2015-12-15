package io.muoncore.spring.integration.stream;

import io.muoncore.Muon;
import io.muoncore.spring.Person;
import io.muoncore.spring.annotations.EnableMuonControllers;
import io.muoncore.spring.integration.MockedMuonConfiguration;
import io.muoncore.spring.model.stream.TestStreamController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MuonStreamControllerIntegrationTest.class, loader = AnnotationConfigContextLoader.class)
@Configuration
@EnableMuonControllers(streamKeepAliveTimeout = 500)
@Import(MockedMuonConfiguration.class)
@ComponentScan(basePackages = "io.muoncore.spring.model.stream")
public class MuonStreamControllerIntegrationTest {
    public static final Person PETER = new Person(123l, "Peter", 23);

    @Autowired
    private Muon muon;
    @Autowired
    private TestStreamController testController;

    TestStreamController mockedTestStreamController = mock(TestStreamController.class);

    @Captor
    private ArgumentCaptor<URI> uriCaptor;
    @Captor
    private ArgumentCaptor<Type> typeCaptor;
    @Captor
    private ArgumentCaptor<Subscriber<Person>> subscriberCaptor;


    @Before
    public void setUp() throws Exception {
        reset(mockedTestStreamController);
        initMocks(this);
        testController.setDelegatingMock(mockedTestStreamController);
    }

    private int findMappingIndex(List<URI> uris, String path) {
        for (int i = 0; i < uris.size(); i++) {
            if (uris.get(i).toString().equals(path)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Stream " + path + " is not registered in muon");
    }


    @Test
    public void subscribesToStreams() throws Exception {
        verifyMuonStreamSetupProcess();

        int i = findMappingIndex(uriCaptor.getAllValues(), "stream://muon-stream-source/personStream");

        assertThat(typeCaptor.getAllValues().get(i), equalTo(Person.class));


        subscriberCaptor.getAllValues().get(i).onNext(PETER);

        verify(mockedTestStreamController, times(1)).addPersonEvent(PETER);
    }

    private void verifyMuonStreamSetupProcess() throws UnsupportedEncodingException {
        verify(muon, times(3)).subscribe(uriCaptor.capture(), typeCaptor.capture(), subscriberCaptor.capture());
    }
}
