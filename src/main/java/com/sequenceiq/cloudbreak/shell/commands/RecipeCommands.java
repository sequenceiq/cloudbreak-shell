package com.sequenceiq.cloudbreak.shell.commands;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import groovyx.net.http.HttpResponseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

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
            @CliOption(key = "url", mandatory = false, help = "URL of the Recipe to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the Recipe") File file,
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

    @CliCommand(value = "recipe store", help = "Create and store a new recipe")
    public String storeRecipe(
            @CliOption(key = "name", mandatory = true, help = "Unique name of the recepie") String name,
            @CliOption(key = "description", help = "Description of the recepie") String description,
            @CliOption(key = "executionType", mandatory = true, help = "Type of recepie execution: ONE_NODE, ALL_NODES") String executionType,
            @CliOption(key = "preInstallScriptFile", help = "Path of the pre install script file") File preInstallScriptFile,
            @CliOption(key = "postInstallScriptFile", help = "Path of the post install script file") File postInstallScriptFile,
            @CliOption(key = "timeout", help = "Timeout of the script execution") Integer timeout,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the recipe is public in the account") Boolean publicInAccount) {
        if (preInstallScriptFile != null && !preInstallScriptFile.exists()) {
            return "Pre install script file not exists.";
        } else if (postInstallScriptFile != null && !postInstallScriptFile.exists()) {
            return "Post install script file not exists.";
        } else if (preInstallScriptFile == null && postInstallScriptFile == null) {
            return "At least one script is required.";
        }
        try {
            String tomlContent = String.format("[plugin]\nname=\"%s\"\ndescription=\"%s\"\nversion=\"1.0\"\n", name, description == null ? "" : description);
            StringBuilder pluginContentBuilder = new StringBuilder()
                .append("plugin.toml:").append(Base64.encodeBase64String(tomlContent.getBytes())).append("\n");
            if (preInstallScriptFile != null) {
                addScriptContent(pluginContentBuilder, "recipe-pre-install", preInstallScriptFile);
            }
            if (postInstallScriptFile != null) {
                addScriptContent(pluginContentBuilder, "recipe-post-install", postInstallScriptFile);
            }

            Map<String, Object> recipe = new HashMap<>();
            recipe.put("name", name);
            recipe.put("description", description);
            recipe.put("timeout", timeout);
            Map<String, String> plugins = new HashMap<>();
            plugins.put("base64://" + Base64.encodeBase64String(pluginContentBuilder.toString().getBytes()), executionType);
            recipe.put("plugins", plugins);

            String id = cloudbreak.postRecipe(jsonMapper.writeValueAsString(recipe), publicInAccount);
            return String.format("Recipe '%s' has been stored with id: %s", name, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private void addScriptContent(StringBuilder builder, String name, File scriptFile) throws IOException {
        String script = IOUtils.toString(new FileInputStream(scriptFile));
        builder.append(name).append(":").append(Base64.encodeBase64String(script.getBytes())).append("\n");
    }

    private String getRecipeName(String json) throws IOException {
        return jsonMapper.readTree(json.getBytes()).get("name").asText();
    }

}
