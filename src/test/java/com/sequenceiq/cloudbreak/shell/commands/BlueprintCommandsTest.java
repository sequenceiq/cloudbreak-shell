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

public class BlueprintCommandsTest {
    private static final String BLUEPRINT_ID = "50";
    private static final String BLUEPRINT_NAME = "dummyName";

    @InjectMocks
    private BlueprintCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    @Mock
    private CloudbreakContext mockContext;

    private Map<String, Object> dummyResult;

    @Before
    public void setUp() {
        underTest = new BlueprintCommands();
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", BLUEPRINT_ID);
    }

    @Test
    public void testSelectBlueprintById() throws Exception {
        given(mockClient.getBlueprint(BLUEPRINT_ID)).willReturn(dummyResult);
        underTest.selectBlueprint(BLUEPRINT_ID, null);
        verify(mockClient, times(1)).getBlueprint(anyString());
        verify(mockContext, times(1)).setHint(Hints.CONFIGURE_INSTANCEGROUP);
    }

    @Test
    public void testSelectBlueprintByIdAndName() throws Exception {
        given(mockClient.getBlueprint(BLUEPRINT_ID)).willReturn(dummyResult);
        underTest.selectBlueprint(BLUEPRINT_ID, BLUEPRINT_NAME);
        verify(mockClient, times(1)).getBlueprint(anyString());
        verify(mockClient, times(0)).getBlueprintByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CONFIGURE_INSTANCEGROUP);
    }

    @Test
    public void testSelectBlueprintByName() throws Exception {
        given(mockClient.getBlueprintByName(BLUEPRINT_NAME)).willReturn(dummyResult);
        underTest.selectBlueprint(null, BLUEPRINT_NAME);
        verify(mockClient, times(1)).getBlueprintByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CONFIGURE_INSTANCEGROUP);
    }

    @Test
    public void testSelectBlueprintWithoutIdAndName() throws Exception {
        underTest.selectBlueprint(null, null);
        verify(mockClient, times(0)).getBlueprintByName(anyString());
        verify(mockClient, times(0)).getBlueprint(anyString());
    }

    @Test
    public void testSelectBlueprintByNameNotFound() throws Exception {
        given(mockClient.getBlueprint(BLUEPRINT_ID)).willReturn(null);
        underTest.selectBlueprint(BLUEPRINT_ID, null);
        verify(mockContext, times(0)).setHint(Hints.CONFIGURE_INSTANCEGROUP);
    }

    @Test
    public void testShowBlueprintById() throws Exception {
        given(mockClient.getBlueprintMap(BLUEPRINT_ID)).willReturn(dummyResult);
        underTest.showBlueprint(BLUEPRINT_ID, null);
        verify(mockClient, times(0)).getBlueprintByName(anyString());
        verify(mockClient, times(1)).getBlueprintMap(anyString());
    }

    @Test
    public void testShowBlueprintByName() throws Exception {
        given(mockClient.getBlueprintMap(BLUEPRINT_ID)).willReturn(dummyResult);
        given(mockClient.getBlueprintByName(BLUEPRINT_NAME)).willReturn(dummyResult);
        underTest.showBlueprint(null, BLUEPRINT_NAME);
        verify(mockClient, times(1)).getBlueprintByName(anyString());
        verify(mockClient, times(1)).getBlueprintMap(anyString());
    }

    @Test
    public void testShowBlueprintByIdAndName() throws Exception {
        given(mockClient.getBlueprintMap(BLUEPRINT_ID)).willReturn(dummyResult);
        underTest.showBlueprint(BLUEPRINT_ID, BLUEPRINT_NAME);
        verify(mockClient, times(0)).getBlueprintByName(anyString());
        verify(mockClient, times(1)).getBlueprintMap(anyString());
    }

    @Test
    public void testDeleteBlueprintById() throws Exception {
        given(mockClient.deleteBlueprint(BLUEPRINT_ID)).willReturn(dummyResult);
        underTest.deleteBlueprint(BLUEPRINT_ID, null);
        verify(mockClient, times(1)).deleteBlueprint(anyString());
    }

    @Test
    public void testDeleteBlueprintByName() throws Exception {
        given(mockClient.deleteBlueprintByName(BLUEPRINT_NAME)).willReturn(dummyResult);
        underTest.deleteBlueprint(null, BLUEPRINT_NAME);
        verify(mockClient, times(1)).deleteBlueprintByName(anyString());
    }

}
