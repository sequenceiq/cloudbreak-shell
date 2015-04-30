package com.sequenceiq.cloudbreak.shell.commands;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.anyString;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

public class StackCommandsTest {
    private static final String STACK_ID = "50";
    private static final String STACK_NAME = "dummyName";

    @InjectMocks
    private StackCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    @Mock
    private CloudbreakContext mockContext;

    private Map<String, Object> dummyResult;

    @Before
    public void setUp() {
        underTest = new StackCommands();
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", STACK_ID);
        dummyResult.put("name", STACK_NAME);
        given(mockContext.isCredentialAvailable()).willReturn(true);
    }

    @Test
    public void testSelectStackById() throws Exception {
        given(mockClient.getStack(STACK_ID)).willReturn(dummyResult);
        underTest.selectStack(STACK_ID, null);
        verify(mockClient, times(1)).getStack(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_CLUSTER);
    }

    @Test
    public void testSelectStackByName() throws Exception {
        given(mockClient.getStackByName(STACK_NAME)).willReturn(dummyResult);
        underTest.selectStack(null, STACK_NAME);
        verify(mockClient, times(1)).getStackByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_CLUSTER);
    }

    @Test
    public void testSelectStackByIdAndName() throws Exception {
        given(mockClient.getStack(STACK_ID)).willReturn(dummyResult);
        underTest.selectStack(STACK_ID, STACK_NAME);
        verify(mockClient, times(1)).getStack(anyString());
        verify(mockClient, times(0)).getStackByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_CLUSTER);
    }

    @Test
    public void testSelectStackNotFoundByName() throws Exception {
        given(mockClient.getStackByName(STACK_NAME)).willReturn(null);
        underTest.selectStack(null, STACK_NAME);
        verify(mockContext, times(0)).setHint(Hints.CREATE_CLUSTER);
    }

    @Test
    public void testSelectStackWithoutIdAndName() throws Exception {
        underTest.selectStack(null, null);
        verify(mockContext, times(0)).setHint(Hints.CREATE_CLUSTER);
    }

    @Test
    public void testTerminateStackById() throws Exception {
        given(mockClient.terminateStack(STACK_ID)).willReturn(dummyResult);
        underTest.terminateStack(STACK_ID, null);
        verify(mockContext, times(1)).removeStack(anyString());
        verify(mockClient, times(0)).getStackByName(anyString());
    }

    @Test
    public void testTerminateStackByName() throws Exception {
        given(mockClient.getStackByName(STACK_NAME)).willReturn(dummyResult);
        given(mockClient.terminateStack(STACK_ID)).willReturn(dummyResult);
        underTest.terminateStack(null, STACK_NAME);
        verify(mockContext, times(1)).removeStack(anyString());
        verify(mockClient, times(1)).getStackByName(anyString());
    }

    @Test
    public void testTerminateStackByIdAndName() throws Exception {
        given(mockClient.terminateStack(STACK_ID)).willReturn(dummyResult);
        underTest.terminateStack(STACK_ID, STACK_NAME);
        verify(mockContext, times(1)).removeStack(anyString());
        verify(mockClient, times(0)).getStackByName(anyString());
    }

    @Test
    public void testTerminateWithoutStackIdAndName() throws Exception {
        underTest.terminateStack(null, null);
        verify(mockContext, times(0)).removeStack(anyString());
    }
}
