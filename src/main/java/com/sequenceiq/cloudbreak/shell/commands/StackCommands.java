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
public class StackCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "stack list")
    public boolean isStackListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack terminate")
    public boolean isStackTerminateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack create")
    public boolean isStackCreateCommandAvailable() {
        return context.isTemplateAvailable() && context.isCredentialAvailable();
    }

    @CliAvailabilityIndicator(value = "stack show")
    public boolean isStackShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack select")
    public boolean isStackSelectCommandAvailable() {
        return true;
    }

    @CliCommand(value = "stack create", help = "Create a new stack based on a template")
    public String createStack(
            @CliOption(key = "nodeCount", mandatory = true, help = "Number of nodes to create") String count,
            @CliOption(key = "name", mandatory = true, help = "Name of the stack") String name) {
        String id = cloudbreak.postStack(name, count, context.getCredentialId(), context.getTemplateId());
        context.addStack(id);
        context.setHint(Hints.CREATE_CLUSTER);
        return "Stack created, id: " + id;
    }

    @CliCommand(value = "stack select", help = "Select the stack by its id")
    public String selectStack(
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        if (cloudbreak.getStack(id) != null) {
            context.addStack(id);
            context.setHint(Hints.CREATE_CLUSTER);
            return "Stack selected, id: " + id;
        } else {
            return "No stack specified";
        }
    }

    @CliCommand(value = "stack terminate", help = "Terminate the stack by its id")
    public String terminateStack(
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        try {
            cloudbreak.terminateStack(id);
            context.setHint(Hints.CREATE_STACK);
            context.removeStack(id);
            return "Stack terminated with id:" + id ;
        } catch (Exception ex) {
            return "Stack terminated was not success with id:" + id ;
        }
    }

    @CliCommand(value = "stack list", help = "Shows all of your stack")
    public String listStacks() {
        return renderSingleMap(cloudbreak.getStacksMap(), "ID", "INFO");
    }

    @CliCommand(value = "stack show", help = "Shows the stack by its id")
    public Object showStack(
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        return renderSingleMap(cloudbreak.getStackMap(id), "FIELD", "VALUE");
    }


}
