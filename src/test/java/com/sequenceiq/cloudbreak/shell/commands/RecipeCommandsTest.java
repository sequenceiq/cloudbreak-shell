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

public class RecipeCommandsTest {
    private static final String RECIPE_ID = "50";
    private static final String RECIPE_NAME = "dummyName";

    @InjectMocks
    private RecipeCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    @Mock
    private CloudbreakContext mockContext;

    private Map<String, Object> dummyResult;

    @Before
    public void setUp() {
        underTest = new RecipeCommands();
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", RECIPE_ID);
    }

    @Test
    public void testSelectRecipeById() throws Exception {
        given(mockClient.getRecipe(RECIPE_ID)).willReturn(dummyResult);
        underTest.selectRecipe(RECIPE_ID, null);
        verify(mockClient, times(1)).getRecipe(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_STACK);
    }

    @Test
    public void testSelectRecipeByName() throws Exception {
        given(mockClient.getRecipeByName(RECIPE_NAME)).willReturn(dummyResult);
        underTest.selectRecipe(null, RECIPE_NAME);
        verify(mockClient, times(1)).getRecipeByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_STACK);
    }

    @Test
    public void testSelectRecipeByIdAndName() throws Exception {
        given(mockClient.getRecipe(RECIPE_ID)).willReturn(dummyResult);
        underTest.selectRecipe(RECIPE_ID, RECIPE_NAME);
        verify(mockClient, times(1)).getRecipe(anyString());
        verify(mockClient, times(0)).getRecipeByName(anyString());
        verify(mockContext, times(1)).setHint(Hints.CREATE_STACK);
    }

    @Test
    public void testSelectRecipeByNameNotFound() throws Exception {
        given(mockClient.getRecipeByName(RECIPE_NAME)).willReturn(null);
        underTest.selectRecipe(null, RECIPE_NAME);
        verify(mockContext, times(0)).setHint(Hints.CREATE_STACK);
    }

    @Test
    public void testSelectRecipeWithoutIdAndName() throws Exception {
        underTest.selectRecipe(null, null);
        verify(mockContext, times(0)).setHint(Hints.CREATE_STACK);
    }

    @Test
    public void testShowRecipeById() throws Exception {
        given(mockClient.getRecipeMap(RECIPE_ID)).willReturn(dummyResult);
        underTest.showRecipe(RECIPE_ID, null);
        verify(mockClient, times(1)).getRecipeMap(anyString());
    }

    @Test
    public void testShowRecipeByName() throws Exception {
        given(mockClient.getRecipeByName(RECIPE_NAME)).willReturn(dummyResult);
        given(mockClient.getRecipeMap(RECIPE_ID)).willReturn(dummyResult);
        underTest.showRecipe(null, RECIPE_NAME);
        verify(mockClient, times(1)).getRecipeMap(anyString());
        verify(mockClient, times(1)).getRecipeByName(anyString());
    }

    @Test
    public void testShowRecipeWithoutIdAndName() throws Exception {
        underTest.selectRecipe(null, null);
        verify(mockClient, times(0)).getRecipeMap(anyString());
    }

    @Test
    public void testDeleteRecipeById() throws Exception {
        given(mockClient.deleteRecipe(RECIPE_ID)).willReturn(dummyResult);
        underTest.deleteRecipe(RECIPE_ID, null);
        verify(mockClient, times(1)).deleteRecipe(anyString());
    }

    @Test
    public void testDeleteRecipeByName() throws Exception {
        given(mockClient.deleteRecipeByName(RECIPE_NAME)).willReturn(dummyResult);
        underTest.deleteRecipe(null, RECIPE_NAME);
        verify(mockClient, times(1)).deleteRecipeByName(anyString());
    }

    @Test
    public void testDeleteRecipeByIdAndName() throws Exception {
        given(mockClient.deleteRecipe(RECIPE_ID)).willReturn(dummyResult);
        underTest.deleteRecipe(RECIPE_ID, RECIPE_NAME);
        verify(mockClient, times(0)).deleteRecipeByName(anyString());
        verify(mockClient, times(1)).deleteRecipe(anyString());
    }

    @Test
    public void testDeleteRecipeWithoutIdAndName() throws Exception {
        underTest.deleteRecipe(null, null);
        verify(mockClient, times(0)).deleteRecipeByName(anyString());
        verify(mockClient, times(0)).deleteRecipe(anyString());
    }
}
