package io.muoncore.spring.annotationprocessor;

import io.muoncore.spring.annotations.PhotonStreamListener;
import io.muoncore.spring.annotations.StreamType;
import io.muoncore.spring.mapping.MuonStreamSubscriptionService;
import io.muoncore.spring.methodinvocation.MuonStreamMethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.util.ReflectionUtils.findMethod;

@RunWith(MockitoJUnitRunner.class)
public class PhotonStreamListenerMethodAnnotationProcessorTest {

    private PhotonStreamListenerMethodAnnotationProcessor processor = new PhotonStreamListenerMethodAnnotationProcessor();

    @Mock
    private MuonStreamSubscriptionService streamSubscrioptionService;

    private TestingMethodHolder testingMethodHolder = new TestingMethodHolder();

    @Before
    public void setUp() throws Exception {
        processor.setStreamSubscriptionService(streamSubscrioptionService);
    }

    @Test
    public void processorGeneratesUrlCorrectly() throws Exception {
        processor.processMethod(findMethod(TestingMethodHolder.class, "photonListener"), testingMethodHolder);

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("stream-name", "the/stream-.!@#%^name");
        expectedParams.put("stream-type", "hot");
        expectedParams.put("from", "10");
        verify(streamSubscrioptionService).setupMuonMapping(eq("muon://photon/stream"), eq(expectedParams), any(MuonStreamMethodInvocation.class));
    }

    private static class TestingMethodHolder {
        @PhotonStreamListener(streamName = "the/stream-.!@#%^name", streamType = StreamType.HOT, from = 10)
        public void photonListener() {
        }
    }
}
