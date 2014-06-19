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
public class CredentialCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "credential list")
    public boolean isCredentialListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential show")
    public boolean isCredentialShowCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential show", help = "Shows the credential by its id")
    public Object showBlueprint(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        return cloudbreak.getCredential(id);
    }

    @CliCommand(value = "credential list", help = "Shows all of your credentials")
    public String listCredentials() {
        return renderSingleMap(cloudbreak.getCredentialsMap(), "ID", "INFO");
    }

    @CliAvailabilityIndicator(value = "credential createEC2")
    public boolean isCredentialEc2CreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential createEC2", help = "Create a new EC2 credential")
    public String createEc2Credential() {
        String id = cloudbreak.postCredentials();
        context.setCredential(id);
        context.setHint(Hints.ADD_BLUEPRINT);
        return "Credential created, id: " + id;
    }

    @CliAvailabilityIndicator(value = "credential createAzure")
    public boolean isCredentialAzureCreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential createAzure", help = "Create a new Azure credential")
    public String createAzureCredential() {
        return "Not supported yet";
    }
}
