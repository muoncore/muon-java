package io.muoncore.spring.methodinvocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.util.ReflectionUtils.findMethod;

@RunWith(MockitoJUnitRunner.class)
public class MuonStreamMethodInvocationTest {
    public static final String SAMPLE_OUTPUT_STRING = "the-string";
    public static final String SAMPLE_INPUT_STRING = "SAMPLE_INPUT_STRING";
    private MuonStreamMethodInvocation methodInvocation;

    @Mock
    private MethodHolder methodHolder;

    @Test
    public void shouldInvokeMethodWithoutParameters() throws Exception {
        when(methodHolder.methodWithoutParameters()).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonStreamMethodInvocation(
                findMethod(MethodHolder.class, "methodWithoutParameters"), methodHolder);
        assertThat(methodInvocation.invoke("String"), is(SAMPLE_OUTPUT_STRING));
        verify(methodHolder, times(1)).methodWithoutParameters();
    }

    @Test
    public void shouldInvokeMethodWithOneParameter() throws Exception {
        when(methodHolder.methodWithSingleStringParameter(SAMPLE_INPUT_STRING)).thenReturn(SAMPLE_OUTPUT_STRING);
        methodInvocation = new MuonStreamMethodInvocation(
                findMethod(MethodHolder.class, "methodWithSingleStringParameter", String.class), methodHolder);
        assertThat(methodInvocation.invoke(SAMPLE_INPUT_STRING), is(SAMPLE_OUTPUT_STRING));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionOnMoreThanOneParameter() throws Exception {
        methodInvocation = new MuonStreamMethodInvocation(
                findMethod(MethodHolder.class, "methodWithTwoStringParameters", String.class, String.class), methodHolder);
        methodHolder.methodWithTwoStringParameters("", "");
    }

    private interface MethodHolder {
        String methodWithoutParameters();

        String methodWithSingleStringParameter(String parameter);

        String methodWithTwoStringParameters(String parameter1, String parameter2);
    }
}