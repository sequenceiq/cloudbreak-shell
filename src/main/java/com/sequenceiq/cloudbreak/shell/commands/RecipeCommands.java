package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

import groovyx.net.http.HttpResponseException;

@Component
public class RecipeCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private ObjectMapper jsonMapper;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "recipe list")
    public boolean isRecipeListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "recipe select")
    public boolean isRecipeSelectCommandAvailable() throws Exception {
        return context.isRecipeAccessible();
    }

    @CliAvailabilityIndicator(value = "recipe add")
    public boolean isRecipeAddCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "recipe show")
    public boolean isRecipeShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "recipe delete")
    public boolean isRecipeDeleteCommandAvailable() {
        return true;
    }

    @CliCommand(value = "recipe list", help = "Shows the currently available recipes")
    public String listRecipes() {
        try {
            return renderSingleMap(cloudbreak.getAccountRecipesMap(), "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe add", help = "Add a new recipe with either --url or --file")
    public String addRecipe(
            @CliOption(key = "url", mandatory = false, help = "URL of the blueprint to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the blueprint") File file,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the recipe is public in the account") Boolean publicInAccount) {
        try {
            String json = file == null ? IOUtils.toString(new URL(url)) : IOUtils.toString(new FileInputStream(file));
            String id = cloudbreak.postRecipe(json, publicInAccount);
            return String.format("Recipe '%s' has been added with id: %s", getRecipeName(json), id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe select", help = "Select the recipe by its id")
    public String selectRecipe(
            @CliOption(key = "id", mandatory = true, help = "Id of the recipe") String id) {
        String message;
        try {
            if (cloudbreak.getRecipe(id) != null) {
                context.addRecipe(id);
                context.setHint(Hints.CREATE_STACK);
                message = String.format("Recipe has been selected, id: %s", id);
            } else {
                message = String.format("Recipe '%s' cannot be found.", id);
            }
            return message;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe show", help = "Shows the properties of the specified recipe")
    public Object showRecipe(
            @CliOption(key = "id", mandatory = true, help = "Id of the recipe") String id) {
        try {
            Map<String, String> map = new HashMap<>();
            List<String> plugins = new ArrayList<>();
            Map<String, String> properties = new HashMap<>();
            Map<String, Object> recipeMap = cloudbreak.getRecipeMap(id);

            for (Map.Entry<String, Object> propertyEntry : recipeMap.entrySet()) {
                if ("plugins".equals(propertyEntry.getKey().toString())) {
                    plugins = (List<String>) propertyEntry.getValue();
                    map.put(propertyEntry.getKey(), plugins.toString());
                } else if ("properties".equals(propertyEntry.getKey().toString())) {
                    properties = (Map<String, String>) propertyEntry.getValue();
                } else {
                    map.put(propertyEntry.getKey(), propertyEntry.getValue().toString());
                }
            }

            return renderSingleMap(map, "FIELD", "INFO") + "\n\n" + renderSingleMap(properties, "CONSUL-KEY", "VALUE") + "\n\n";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe delete", help = "Delete the recipe by its id")
    public Object deleteRecipe(
            @CliOption(key = "id", mandatory = true, help = "Id of the recipe") String id) {
        try {
            return cloudbreak.deleteRecipe(id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private String getRecipeName(String json) throws IOException {
        return jsonMapper.readTree(json.getBytes()).get("name").asText();
    }

}
