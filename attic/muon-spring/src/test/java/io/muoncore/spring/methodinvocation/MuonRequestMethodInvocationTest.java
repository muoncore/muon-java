package io.muoncore.spring.methodinvocation;

import io.muoncore.protocol.rpc.client.requestresponse.Request;
import io.muoncore.protocol.rpc.client.requestresponse.server.RequestWrapper;
import io.muoncore.protocol.rpc.client.requestresponse.server.ServerRequest;
import io.muoncore.spring.MuonTestUtils;
import io.muoncore.spring.annotations.parameterhandlers.DecodedContent;
import io.muoncore.spring.annotations.parameterhandlers.Parameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.findMethod;

@RunWith(MockitoJUnitRunner.class)
public class MuonRequestMethodInvocationTest {
    public static final String SAMPLE_OUTPUT_STRING = "the-string";
    public static final String SAMPLE_INPUT_STRING = "SAMPLE_INPUT_STRING";
    private MuonRequestMethodInvocation methodInvocation;

    @Mock
    private MethodHolder methodHolder;

    @Test
    public void shouldInvokeMethodWithoutParameters() throws Exception {
        when(methodHolder.methodWithoutParameters()).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "methodWithoutParameters"), methodHolder);
        assertThat(methodInvocation.invoke(MuonTestUtils.getSampleMuonRequestWrapper("ignored")), is(SAMPLE_OUTPUT_STRING));
        verify(methodHolder, times(1)).methodWithoutParameters();
    }

    @Test
    public void shouldMapDecodedContentToMethodParameter() throws Exception {
        when(methodHolder.decodedContentParameter(SAMPLE_INPUT_STRING)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "decodedContentParameter", String.class), methodHolder);
        assertThat(methodInvocation.invoke(MuonTestUtils.getSampleMuonRequestWrapper(SAMPLE_INPUT_STRING)), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapRequestWrapperObjectToMethodParameters() throws Exception {
        RequestWrapper sampleMuonResourceEvent = MuonTestUtils.getSampleMuonRequestWrapper("test");
        when(methodHolder.muonRequestWrapperParameter(sampleMuonResourceEvent)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "muonRequestWrapperParameter", RequestWrapper.class), methodHolder);
        assertThat(methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapRequestObjectToMethodParameters() throws Exception {
        RequestWrapper sampleMuonResourceEvent = MuonTestUtils.getSampleMuonRequestWrapper("test");
        when(methodHolder.muonRequestParameter(sampleMuonResourceEvent.getRequest())).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "muonRequestParameter", Request.class), methodHolder);
        assertThat(methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapSingleMuonParameterToMethodParameters() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", 13);
        RequestWrapper sampleMuonResourceEvent = MuonTestUtils.getSampleMuonRequestWrapper(parameters);
        when(methodHolder.muonSingleParameter(13)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "muonSingleParameter", int.class), methodHolder);
        assertThat(methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    @Test
    public void shouldMapMultipleMuonParametersToMethodParameters() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", 13);
        parameters.put("param2", "test");
        RequestWrapper sampleMuonResourceEvent = MuonTestUtils.getSampleMuonRequestWrapper(parameters);
        when(methodHolder.muonMultipleParameters(13, "test")).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonRequestMethodInvocation(
                findMethod(MethodHolder.class, "muonMultipleParameters", int.class, String.class), methodHolder);
        assertThat(methodInvocation.invoke(sampleMuonResourceEvent), is(SAMPLE_OUTPUT_STRING));
    }

    private interface MethodHolder {
        String methodWithoutParameters();

        String decodedContentParameter(@DecodedContent String parameter);

        String muonRequestWrapperParameter(RequestWrapper parameter);

        String muonRequestParameter(ServerRequest parameter);

        String muonSingleParameter(@Parameter("param1") int parameter);

        String muonMultipleParameters(@Parameter("param1") int parameter1, @Parameter("param2") String parameter2);
    }

}
