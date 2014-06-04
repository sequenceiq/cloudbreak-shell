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

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

@Component
public class BlueprintCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;

    @CliAvailabilityIndicator(value = "blueprint list")
    public boolean isBlueprintListCommandAvailable() {
        return context.isBlueprintAvailable();
    }

    @CliCommand(value = "blueprint list", help = "Shows the currently available blueprints")
    public String listBlueprints() {
        return "blueprint list";
    }

    @CliAvailabilityIndicator(value = "blueprint add")
    public boolean isBlueprintAddCommandAvailable() {
        return true;
    }

    @CliCommand(value = "blueprint add", help = "Add a new blueprint with either --url or --file")
    public String addBlueprint(
            @CliOption(key = "url", mandatory = false, help = "URL of the blueprint to download from") String url,
            @CliOption(key = "file", mandatory = false, help = "File which contains the blueprint") File file) {
        context.setBlueprintAvailable(true);
        context.setHint(Hints.CREATE_TEMPLATE);
        return "message";
    }


}
