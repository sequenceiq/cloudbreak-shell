/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public boolean isBlueprintSelectCommandAvailable() {
        return true;
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
        } catch (Exception e) {
            message = "Failed to add the default blueprints: " + e.getMessage();
        }
        return message;
    }

    @CliCommand(value = "blueprint delete", help = "Delete the blueprint by its id")
    public Object deleteBlueprint(
            @CliOption(key = "id", mandatory = true, help = "Id of the blueprint") String id) {
        return cloudbreak.deleteBlueprint(id);
    }

    @CliCommand(value = "blueprint list", help = "Shows the currently available blueprints")
    public String listBlueprints() {
        return renderSingleMap(cloudbreak.getBlueprintsMap(), "FIELD", "VALUE");
    }

    @CliCommand(value = "blueprint show", help = "Shows the blueprint by its id")
    public Object showBlueprint(
            @CliOption(key = "id", mandatory = true, help = "Id of the blueprint") String id) {
        Map<String, String> map = new HashMap<>();
        Map<String, List<String>> hosts = new HashMap<>();
        Map<String, Object> blueprintMap = cloudbreak.getBlueprintMap(id);

        for (Map.Entry<String, Object> stringStringEntry : blueprintMap.entrySet()) {
            if ("ambariBlueprint".equals(stringStringEntry.getKey().toString())) {
                hosts = (Map<String, List<String>>) stringStringEntry.getValue();
            } else {
                map.put(stringStringEntry.getKey(), stringStringEntry.getValue().toString());
            }
        }

        return renderSingleMap(map, "FIELD", "INFO") + "\n\n" + renderMultiValueMap(hosts, "HOSTGROUP", "COMPONENT");

    }

    @CliCommand(value = "blueprint select", help = "Select the blueprint by its id")
    public String selectBlueprint(
            @CliOption(key = "id", mandatory = true, help = "Id of the blueprint") String id) {
        String message;
        if (cloudbreak.getBlueprint(id) != null) {
            context.addBlueprint(id);
            context.setHint(Hints.CREATE_STACK);
            message = String.format("Blueprint has been selected, id: %s", id);
        } else {
            message = "No blueprint specified";
        }
        return message;
    }

    @CliCommand(value = "blueprint add", help = "Add a new blueprint with either --url or --file")
    public String addBlueprint(
            @CliOption(key = "description", mandatory = true, help = "Description of the blueprint to download from") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the blueprint to download from") String name,
            @CliOption(key = "url", mandatory = false, help = "URL of the blueprint to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the blueprint") File file) {
        String message;
        String json = file == null ? readContent(url) : readContent(file);
        if (json != null) {
            String id = cloudbreak.postBlueprint(name, description, json);
            context.addBlueprint(id);
            context.setHint(Hints.CREATE_STACK);
            message = String.format("Blueprint: '%s' has been added, id: %s", getBlueprintName(json), id);
        } else {
            message = "No blueprint specified";
        }
        return message;
    }

    private String readContent(File file) {
        String content = null;
        try {
            content = IOUtils.toString(new FileInputStream(file));
        } catch (IOException e) {
            // not important
        }
        return content;
    }

    private String readContent(String url) {
        String content = null;
        try {
            content = IOUtils.toString(new URL(url));
        } catch (IOException e) {
            // not important
        }
        return content;
    }

    private String getBlueprintName(String json) {
        String result = "";
        try {
            result = jsonMapper.readTree(json.getBytes()).get("Blueprints").get("blueprint_name").asText();
        } catch (IOException e) {
            // not important
        }
        return result;
    }
}
