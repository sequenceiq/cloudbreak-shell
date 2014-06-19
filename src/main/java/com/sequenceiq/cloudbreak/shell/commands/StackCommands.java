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

    @CliAvailabilityIndicator(value = "stack create")
    public boolean isStackCreateCommandAvailable() {
        return context.isCredentialAvailable();
    }

    @CliCommand(value = "stack create", help = "Create a new stack based on a template")
    public String createStack(
            @CliOption(key = "nodeCount", mandatory = true, help = "Number of nodes to create") String count,
            @CliOption(key = "name", mandatory = true, help = "Name of the stack") String name) {
        String id = cloudbreak.postStack(name, count);
        context.addStack(id);
        context.setHint(Hints.CREATE_CLUSTER);
        return "Stack created, id: " + id;
    }

    @CliCommand(value = "stack list", help = "Shows all of your stack")
    public String listStacks() {
        return renderSingleMap(cloudbreak.getStacksMap(), "ID", "INFO");
    }
}
