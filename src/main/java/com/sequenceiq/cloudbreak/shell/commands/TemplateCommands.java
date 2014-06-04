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
public class TemplateCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "template list")
    public boolean isTemplateListCommandAvailable() {
        return context.isTemplateAvailable();
    }

    @CliCommand(value = "template list", help = "Shows the currently available cloud templates")
    public String listTemplates() {
        return "template list";
    }

    @CliAvailabilityIndicator(value = "template create")
    public boolean isTemplateCreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "template create", help = "Create a new cloud template")
    public String createTemplate(@CliOption(key = "name", mandatory = true, help = "Name of the cloud template") String name) {
        String id = cloudbreak.postTemplate(name);
        context.addTemplate(id);
        context.setHint(Hints.CREATE_STACK);
        return "Template created, id: " + id;
    }
}
