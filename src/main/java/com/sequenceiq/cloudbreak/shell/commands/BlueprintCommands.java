package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderMultiValueMap;
import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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
public class BlueprintCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private ObjectMapper jsonMapper;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "blueprint list")
    public boolean isBlueprintListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "blueprint select")
    public boolean isBlueprintSelectCommandAvailable() throws Exception {
        return context.isBlueprintAccessible();
    }

    @CliAvailabilityIndicator(value = "blueprint add")
    public boolean isBlueprintAddCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "blueprint show")
    public boolean isBlueprintShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "blueprint delete")
    public boolean isBlueprintDeleteCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "blueprint defaults")
    public boolean isBlueprintDefaultsAddCommandAvailable() {
        return true;
    }

    @CliCommand(value = "blueprint defaults", help = "Adds the default blueprints to Ambari")
    public String addBlueprint() {
        String message = "Default blueprints added";
        try {
            cloudbreak.addDefaultBlueprints();
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception e) {
            message = "Failed to add the default blueprints: " + e.getMessage();
        }
        return message;
    }

    @CliCommand(value = "blueprint delete", help = "Delete the blueprint by its id or name")
    public Object deleteBlueprint(
            @CliOption(key = "id", mandatory = false, help = "Id of the blueprint") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the blueprint") String name) {
        try {
            if (id != null) {
                return cloudbreak.deleteBlueprint(id);
            } else if (name != null) {
                return cloudbreak.deleteBlueprintByName(name);
            } else {
                return "No blueprint specified (select a blueprint by --id or --name)";
            }
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "blueprint list", help = "Shows the currently available blueprints")
    public String listBlueprints() {
        try {
            return renderSingleMap(cloudbreak.getAccountBlueprintsMap(), "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "blueprint show", help = "Shows the blueprint by its id or name")
    public Object showBlueprint(
            @CliOption(key = "id", mandatory = false, help = "Id of the blueprint") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the blueprint") String name) {
        try {
            Map<String, Object> blueprintMap = null;
            if (id != null) {
                blueprintMap = cloudbreak.getBlueprintMap(id);
            } else if (name != null) {
                Object blueprint = cloudbreak.getBlueprintByName(name);
                if (blueprint != null) {
                    Map<String, Object> blMap = (Map<String, Object>) blueprint;
                    String blueprintId = blMap.get("id").toString();
                    blueprintMap = cloudbreak.getBlueprintMap(blueprintId);
                } else {
                    return "No blueprints specified.";
                }
            } else {
                return "No blueprints specified.";
            }
            Map<String, String> map = new HashMap<>();
            Map<String, List<String>> hosts = new HashMap<>();

            for (Map.Entry<String, Object> stringStringEntry : blueprintMap.entrySet()) {
                if ("ambariBlueprint".equals(stringStringEntry.getKey().toString())) {
                    hosts = (Map<String, List<String>>) stringStringEntry.getValue();
                } else {
                    map.put(stringStringEntry.getKey(), stringStringEntry.getValue().toString());
                }
            }

            return renderSingleMap(map, "FIELD", "INFO") + "\n\n" + renderMultiValueMap(hosts, "HOSTGROUP", "COMPONENT");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }

    }

    @CliCommand(value = "blueprint select", help = "Select the blueprint by its id or name")
    public String selectBlueprint(
            @CliOption(key = "id", mandatory = false, help = "Id of the blueprint") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the blueprint") String name) {
        try {
            if (id != null) {
                if (cloudbreak.getBlueprint(id) != null) {
                    context.addBlueprint(id);
                    context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
                    return String.format("Blueprint has been selected, id: %s", id);
                }
            } else if (name != null) {
                Object blueprint = cloudbreak.getBlueprintByName(name);
                if (blueprint != null) {
                    Map<String, Object> blueprintMap = (Map<String, Object>) blueprint;
                    context.addBlueprint(blueprintMap.get("id").toString());
                    context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
                    return String.format("Blueprint has been selected, name: %s", name);
                }
            }
            return "No blueprint specified (select a blueprint by --id or --name)";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "blueprint add", help = "Add a new blueprint with either --url or --file")
    public String addBlueprint(
            @CliOption(key = "description", mandatory = true, help = "Description of the blueprint to download from") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the blueprint to download from") String name,
            @CliOption(key = "url", mandatory = false, help = "URL of the blueprint to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the blueprint") File file,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the blueprint is public in the account") Boolean publicInAccount) {
        try {
            String message;
            String json = file == null ? IOUtils.toString(new URL(url)) : IOUtils.toString(new FileInputStream(file));
            if (json != null) {
                String id = cloudbreak.postBlueprint(name, description, json, publicInAccount);
                context.addBlueprint(id);
                if (cloudbreak.getAccountStacks().isEmpty()) {
                    context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
                } else {
                    context.setHint(Hints.SELECT_STACK);
                }
                message = String.format("Blueprint: '%s' has been added, id: %s", getBlueprintName(json), id);
            } else {
                message = "No blueprint specified";
            }
            return message;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private String getBlueprintName(String json) {
        String result = "";
        try {
            result = jsonMapper.readTree(json.getBytes()).get("Blueprints").get("blueprint_name").asText();
        } catch (IOException e) {
            e.toString();
        }
        return result;
    }
}
