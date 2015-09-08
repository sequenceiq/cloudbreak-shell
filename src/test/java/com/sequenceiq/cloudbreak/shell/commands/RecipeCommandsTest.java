package com.sequenceiq.cloudbreak.shell.commands;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

public class RecipeCommandsTest {
    private static final String RECIPE_ID = "50";
    private static final String RECIPE_NAME = "dummyName";

    @InjectMocks
    private RecipeCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    @Mock
    private CloudbreakContext mockContext;

    @Mock
    private ObjectMapper jsonMapper;

    private Map<String, Object> dummyResult;

    @Before
    public void setUp() {
        underTest = new RecipeCommands();
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", RECIPE_ID);
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
        underTest.showRecipe(null, null);
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

    @Test
    public void testStoreRecipePreScriptExists() throws Exception {
        underTest.storeRecipe("name", null, "ALL_NODES", new File(getClass().getResource("/store-recipe-test").getFile()), null, null, null);
        verify(mockClient, times(1)).postRecipe(anyString(), anyBoolean());
    }

    @Test
    public void testStoreRecipePostScriptExists() throws Exception {
        underTest.storeRecipe("name", null, "ALL_NODES", null, new File(getClass().getResource("/store-recipe-test").getFile()), null, null);
        verify(mockClient, times(1)).postRecipe(anyString(), anyBoolean());
    }

    @Test
    public void testStoreRecipeMissingScriptFiles() throws Exception {
        underTest.storeRecipe("name", null, "ALL_NODES", null, null, null, null);
        verify(mockClient, times(0)).postRecipe(anyString(), anyBoolean());
    }

    @Test
    public void testStoreRecipeNotExistsPreScriptFile() throws Exception {
        underTest.storeRecipe("name", null, "ALL_NODES", new File(""), null, null, null);
        verify(mockClient, times(0)).postRecipe(anyString(), anyBoolean());
    }

    @Test
    public void testStoreRecipeNotExistsPostScriptFile() throws Exception {
        underTest.storeRecipe("name", null, "ALL_NODES", null, new File(""), null, null);
        verify(mockClient, times(0)).postRecipe(anyString(), anyBoolean());
    }
}
