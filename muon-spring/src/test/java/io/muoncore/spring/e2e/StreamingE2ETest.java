package io.muoncore.spring.e2e;

import io.muoncore.protocol.reactivestream.server.PublisherLookup;
import io.muoncore.protocol.reactivestream.server.ReactiveStreamServerHandlerApi;
import io.muoncore.spring.e2e.stream.StreamListenerServiceConfiguration;
import io.muoncore.spring.e2e.stream.StreamSourceServiceConfiguration;
import io.muoncore.spring.model.stream.TestStreamController;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reactor.core.processor.CancelException;
import reactor.rx.broadcast.Broadcaster;
import io.muoncore.spring.*;

import static org.mockito.Mockito.*;

@Ignore("Pending fix of MutiBootSpec")
public class StreamingE2ETest {

    private static TestStreamController testControllerDelegatingMock;
    private static Broadcaster<Person> personSource;
    private static ReactiveStreamServerHandlerApi reactiveStreamServerApi;

    @BeforeClass
    public static void setUpContext() throws Exception {
        testControllerDelegatingMock = Mockito.mock(TestStreamController.class);
        ApplicationContext streamSourceServiceContext = new AnnotationConfigApplicationContext(StreamSourceServiceConfiguration.class);
        reactiveStreamServerApi = streamSourceServiceContext.getBean(ReactiveStreamServerHandlerApi.class);
        personSource = Broadcaster.create();

        reactiveStreamServerApi.publishSource("personStream", PublisherLookup.PublisherType.HOT, personSource);

        ApplicationContext streamListenerServiceContext = new AnnotationConfigApplicationContext(StreamListenerServiceConfiguration.class);

        streamListenerServiceContext
                .getBean(TestStreamController.class)
                .setDelegatingMock(testControllerDelegatingMock);
    }

    @Before
    public void setUp() throws Exception {
        Mockito.reset(testControllerDelegatingMock);
    }

    @Test
    public void performsSimpleRequest() throws Exception {
        Person aPerson = PersonBuilder.aDefaultPerson().build();
        personSource.accept(aPerson);

        Thread.sleep(100);

        verify(testControllerDelegatingMock, times(1)).addPersonEvent(eq(aPerson));
    }

    @Test(expected = CancelException.class)
    public void throwsExceptionIfPublishToNonExistentStream() throws Exception {
        Broadcaster<Person> personBroadcaster = Broadcaster.create();
        reactiveStreamServerApi.publishSource("the-wrong-source", PublisherLookup.PublisherType.HOT, personBroadcaster);

        Person aPerson = PersonBuilder.aDefaultPerson().build();
        personBroadcaster.accept(aPerson);
    }

    @Test
    public void reconnectsAfterBroadcasterFails() throws Exception {

        final Broadcaster<Person> removePersonStream = Broadcaster.create();
        reactiveStreamServerApi.publishSource("removePersonStream", PublisherLookup.PublisherType.HOT, removePersonStream);

        Thread.sleep(200);

        Person aPerson = PersonBuilder.aDefaultPerson().build();
        removePersonStream.accept(aPerson);

        Thread.sleep(100);

        verify(testControllerDelegatingMock, times(1)).removePersonEvent(eq(aPerson));
    }
}