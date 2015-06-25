package io.muoncore.spring.methodinvocation;

import io.muoncore.spring.annotations.parameterhandlers.DecodedContent;
import io.muoncore.spring.annotations.parameterhandlers.MuonHeader;
import io.muoncore.spring.annotations.parameterhandlers.MuonHeaders;
import io.muoncore.transport.resource.MuonResourceEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.findMethod;

@RunWith(MockitoJUnitRunner.class)
public class MuonResourceMethodInvocationTest {
    public static final String SAMPLE_OUTPUT_STRING = "the-string";
    public static final String SAMPLE_INPUT_STRING = "SAMPLE_INPUT_STRING";
    private MuonResourceMethodInvocation methodInvocation;

    @Mock
    private MethodHolder methodHolder;

    @Test
    public void shouldInvokeMethodWithoutParameters() throws Exception {
        when(methodHolder.methodWithoutParameters()).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "methodWithoutParameters"), methodHolder);
        assertThat((String) methodInvocation.invoke(getSampleMuonResourceEvent()), is(SAMPLE_OUTPUT_STRING));
        verify(methodHolder, times(1)).methodWithoutParameters();
    }

    private MuonResourceEvent<String> getSampleMuonResourceEvent() throws URISyntaxException {
        return new MuonResourceEvent<String>(new URI("muon://service/resource"));
    }

    @Test
    public void shouldMapDecodedContentToMethodParameter() throws Exception {
        when(methodHolder.decodedContentParameter(SAMPLE_INPUT_STRING)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "decodedContentParameter", String.class), methodHolder);
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        sampleMuonResourceEvent.setDecodedContent(SAMPLE_INPUT_STRING);
        assertThat((String) methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapMuonHeadersToMethodParameters() throws Exception {

        HashMap<String, String> muonHeaders = new HashMap<>();
        muonHeaders.put("Content-type", "text/html");
        muonHeaders.put("param1", "value1");
        when(methodHolder.muonHeadersParameter(eq(muonHeaders))).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "muonHeadersParameter", Map.class), methodHolder);
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        sampleMuonResourceEvent.addHeader("Content-type", "text/html");
        sampleMuonResourceEvent.addHeader("param1", "value1");

        assertThat((String) methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapMuonResourceEventToMethodParameters() throws Exception {
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        when(methodHolder.muonResourceEventParameter(sampleMuonResourceEvent)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "muonResourceEventParameter", MuonResourceEvent.class), methodHolder);

        assertThat((String) methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapMultipleArgumentsToMethodParameters() throws Exception {
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        sampleMuonResourceEvent.addHeader("Content-type", "text/html");
        sampleMuonResourceEvent.addHeader("param1", "value1");
        HashMap<String, String> muonHeaders = new HashMap<>();
        muonHeaders.put("Content-type", "text/html");
        muonHeaders.put("param1", "value1");
        sampleMuonResourceEvent.setDecodedContent(SAMPLE_INPUT_STRING);

        when(methodHolder.multipleParameters(eq(SAMPLE_INPUT_STRING), eq(muonHeaders), eq(sampleMuonResourceEvent))).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "multipleParameters", String.class, Map.class, MuonResourceEvent.class), methodHolder);

        assertThat((String) methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapMuonSingleHeaderToMethodParameters() throws Exception {
        final String HEADER_VALUE = "value1";
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "muonSingleHeaderParameter", String.class), methodHolder);
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        sampleMuonResourceEvent.addHeader("Content-type", "text/html");
        sampleMuonResourceEvent.addHeader("param1", HEADER_VALUE);

        methodInvocation.invoke(sampleMuonResourceEvent);

        verify(methodHolder, times(1)).muonSingleHeaderParameter(HEADER_VALUE);
    }

    @Test
    public void shouldMapMuonSingleHeaderToNullIfNotPresentMethodParameters() throws Exception {
        methodInvocation = new MuonResourceMethodInvocation(
                findMethod(MethodHolder.class, "muonSingleHeaderParameter", String.class), methodHolder);
        MuonResourceEvent<String> sampleMuonResourceEvent = getSampleMuonResourceEvent();
        sampleMuonResourceEvent.addHeader("Content-type", "text/html");

        methodInvocation.invoke(sampleMuonResourceEvent);

        verify(methodHolder, times(1)).muonSingleHeaderParameter(null);
    }

    private interface MethodHolder {
        String methodWithoutParameters();

        String decodedContentParameter(@DecodedContent String parameter);

        String muonHeadersParameter(@MuonHeaders Map parameter);

        String muonSingleHeaderParameter(@MuonHeader("param1") String parameter);

        String muonResourceEventParameter(MuonResourceEvent parameter);

        String multipleParameters(@DecodedContent String parameter1, @MuonHeaders Map parameter2, MuonResourceEvent parameter3);
    }

}