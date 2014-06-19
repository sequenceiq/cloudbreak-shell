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

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

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

    @CliCommand(value = "blueprint list", help = "Shows the currently available blueprints")
    public String listBlueprints() {
        return renderSingleMap(cloudbreak.getBlueprintsMap(), "ID", "INFO");
    }

    @CliAvailabilityIndicator(value = "blueprint add")
    public boolean isBlueprintAddCommandAvailable() {
        return true;
    }

    @CliCommand(value = "blueprint add", help = "Add a new blueprint with either --url or --file")
    public String addBlueprint(
            @CliOption(key = "url", mandatory = false, help = "URL of the blueprint to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the blueprint") File file) {
        String message;
        String json = file == null ? readContent(url) : readContent(file);
        if (json != null) {
            String id = cloudbreak.postBlueprint(json);
            context.addBlueprint(id);
            context.setHint(Hints.CREATE_TEMPLATE);
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
