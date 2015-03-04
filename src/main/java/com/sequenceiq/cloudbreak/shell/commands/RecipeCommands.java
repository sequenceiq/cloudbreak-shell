package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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
            context.addRecipe(id);
            return String.format("Recipe '%s' has been added with id: %s", getRecipeName(json), id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe select", help = "Select the recipe by its id or name")
    public String selectRecipe(
            @CliOption(key = "id", mandatory = false, help = "Id of the recipe") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the recipe") String name) {
        try {
            if (id != null) {
                if (cloudbreak.getRecipe(id) != null) {
                    context.addRecipe(id);
                    context.setHint(Hints.CREATE_STACK);
                    return String.format("Recipe has been selected, id: %s", id);
                }
            } else if (name != null) {
                Object recipe = cloudbreak.getRecipeByName(name);
                if (recipe != null) {
                    Map<String, Object> recipeMap = (Map<String, Object>) recipe;
                    context.addRecipe(recipeMap.get("id").toString());
                    context.setHint(Hints.CREATE_STACK);
                    return String.format("Recipe has been selected, name: %s", name);
                }
            }
            return String.format("Recipe '%s' cannot be found.", id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe show", help = "Shows the properties of the specified recipe")
    public Object showRecipe(
            @CliOption(key = "id", mandatory = false, help = "Id of the recipe") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the recipe") String name) {
        try {
            Map<String, Object> recipeMap = null;
            if (id != null) {
                recipeMap = cloudbreak.getRecipeMap(id);
            } else if (name != null) {
                Object recipe = cloudbreak.getRecipeByName(name);
                if (recipe != null) {
                    Map<String, Object> rMap = (Map<String, Object>) recipe;
                    String recipeId = rMap.get("id").toString();
                    recipeMap = cloudbreak.getRecipeMap(recipeId);
                } else {
                    return "Recipe not specified.";
                }
            } else {
                return "Recipe not specified.";
            }
            Map<String, String> map = new HashMap<>();
            Map<String, String> plugins = new HashMap<>();
            Map<String, String> properties = new HashMap<>();

            for (Map.Entry<String, Object> propertyEntry : recipeMap.entrySet()) {
                if ("plugins".equals(propertyEntry.getKey().toString())) {
                    plugins = (Map<String, String>) propertyEntry.getValue();
                } else if ("properties".equals(propertyEntry.getKey().toString())) {
                    properties = (Map<String, String>) propertyEntry.getValue();
                } else {
                    if (propertyEntry.getValue() != null) {
                        map.put(propertyEntry.getKey(), propertyEntry.getValue().toString());
                    }
                }
            }

            return renderSingleMap(map, "FIELD", "INFO") + "\n\n"
                    + renderSingleMap(properties, "CONSUL-KEY", "VALUE") + "\n\n"
                    + renderSingleMap(plugins, "PLUGIN", "EXECUTION_TYPE") + "\n\n";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "recipe delete", help = "Delete the recipe by its id or name")
    public Object deleteRecipe(
            @CliOption(key = "id", mandatory = false, help = "Id of the recipe") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the recipe") String name) {
        try {
            if (id != null) {
                return cloudbreak.deleteRecipe(id);
            } else if (name != null) {
                return cloudbreak.deleteRecipeByName(name);
            }
            return "Recipe not specified (select recipe by --id or --name)";
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
