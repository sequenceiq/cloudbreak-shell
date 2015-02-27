package com.sequenceiq.cloudbreak.shell.commands;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.anyString;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

public class TemplateCommandsTest {
    private static final String TEMPLATE_ID = "50";
    private static final String TEMPLATE_NAME = "dummyName";

    @InjectMocks
    private TemplateCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    private Map<String, String> dummyResult;

    @Before
    public void setUp() {
        underTest = new TemplateCommands();
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", TEMPLATE_ID);
    }

    @Test
    public void testShowTemplateById() throws Exception {
        given(mockClient.getTemplateMap(TEMPLATE_ID)).willReturn(dummyResult);
        underTest.showTemplate(TEMPLATE_ID, null);
        verify(mockClient, times(1)).getTemplateMap(anyString());
    }

    @Test
    public void testShowTemplateByName() throws Exception {
        given(mockClient.getTemplateByName(TEMPLATE_NAME)).willReturn(dummyResult);
        given(mockClient.getTemplateMap(TEMPLATE_ID)).willReturn(dummyResult);
        underTest.showTemplate(null, TEMPLATE_NAME);
        verify(mockClient, times(1)).getTemplateMap(anyString());
        verify(mockClient, times(1)).getTemplateByName(anyString());
    }

    @Test
    public void testShowTemplateByNameNotFound() throws Exception {
        given(mockClient.getTemplateByName(TEMPLATE_NAME)).willReturn(null);
        underTest.showTemplate(null, TEMPLATE_NAME);
        verify(mockClient, times(0)).getTemplateMap(anyString());
    }

    @Test
    public void testDeleteTemplateById() throws Exception {
        underTest.deleteTemplate(TEMPLATE_ID, null);
        verify(mockClient, times(1)).deleteTemplate(anyString());
    }

    @Test
    public void testDeleteTemplateByName() throws Exception {
        underTest.deleteTemplate(null, TEMPLATE_NAME);
        verify(mockClient, times(1)).deleteTemplateByName(anyString());
    }

    @Test
    public void testDeleteTemplateWithoutIdAndName() throws Exception {
        underTest.deleteTemplate(null, null);
        verify(mockClient, times(0)).deleteTemplate(anyString());
        verify(mockClient, times(0)).deleteTemplateByName(anyString());
    }
}
