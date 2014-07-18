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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static final String AZURE = "AZURE";
    private List<Map> maps = new ArrayList<>();

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "credential list")
    public boolean isCredentialListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential delete")
    public boolean isCredentialDeleteCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential show")
    public boolean isCredentialShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential defaults")
    public boolean isCredentialDefaultsCommandAvailable() {
        return false;
    }

    @CliAvailabilityIndicator(value = "credential select")
    public boolean isCredentialSelectCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential createEC2")
    public boolean isCredentialEc2CreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential show", help = "Shows the credential by its id")
    public Object showCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        return renderSingleMap(cloudbreak.getCredentialMap(id), "FIELD", "VALUE");
    }

    @CliCommand(value = "credential delete", help = "Delete the credential by its id")
    public Object deleteCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        return cloudbreak.deleteCredential(id);
    }

    @CliCommand(value = "credential list", help = "Shows all of your credentials")
    public String listCredentials() {
        return renderSingleMap(cloudbreak.getCredentialsMap(), "ID", "INFO");
    }

    @CliCommand(value = "credential defaults", help = "Adds the default credentials to Cloudbreak")
    public String addDefaultCredentials() {
        String message = "Default credentials added";
        try {
            cloudbreak.addDefaultCredentials();
        } catch (Exception e) {
            message = "Failed to add the default credentials: " + e.getMessage();
        }
        return message;
    }

    @CliCommand(value = "credential select", help = "Select the credential by its id")
    public String selectCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        if (cloudbreak.getCredential(id) != null) {
            context.setCredential(id);
            context.setHint(Hints.CREATE_TEMPLATE);
            return "Credential selected, id: " + id;
        } else {
            return "No credential specified";
        }
    }

    @CliCommand(value = "credential createEC2", help = "Create a new EC2 credential")
    public String createEc2Credential(
            @CliOption(key = "description", mandatory = true, help = "Description of the credential") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "roleArn", mandatory = true, help = "roleArn of the credential") String roleArn,
            @CliOption(key = "sshKeyPath", mandatory = true, help = "sshKeyPath of the template") String sshKeyPath
    ) {
        if (sshKeyPath.isEmpty() || sshKeyPath == null) {
            return "SshKeyPath cannot be null if password null";
        }
        String sshKey = "";
        try {
            sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath))).replaceAll("\n", "");
        } catch (IOException e) {
            return "File not found with ssh key.";
        }
        String id = cloudbreak.postEc2Credential(name, description, roleArn, sshKey);
        context.setCredential(id);
        context.setHint(Hints.CREATE_TEMPLATE);
        return "Credential created, id: " + id;
    }

    @CliAvailabilityIndicator(value = "credential createAZURE")
    public boolean isCredentialAzureCreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential createAZURE", help = "Create a new AZURE credential")
    public String createAzureCredential(
            @CliOption(key = "description", mandatory = true, help = "Description of the credential") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "subscriptionId", mandatory = true, help = "subscriptionId of the credential") String subscriptionId,
            @CliOption(key = "jksPassword", mandatory = true, help = "jksPassword of the credential") String jksPassword,
            @CliOption(key = "sshKeyPath", mandatory = true, help = "sshKeyPath of the template") String sshKeyPath
    ) {
        if (sshKeyPath.isEmpty() || sshKeyPath == null) {
            return "SshKeyPath cannot be null if password null";
        }
        String sshKey = "";
        try {
            sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath))).replaceAll("\n", "");
        } catch (IOException e) {
            return "File not found with ssh key.";
        }
        String id = cloudbreak.postAzureCredential(name, description, subscriptionId, jksPassword, sshKey);
        context.setCredential(id);
        context.setHint(Hints.CREATE_TEMPLATE);
        return "Credential created, id: " + id;
    }

    @CliAvailabilityIndicator(value = "credential certificate")
    public boolean isCredentialCertificateCommandAvailable() {
        List<Map> credentials = cloudbreak.getCredentials();
        int count = 0;
        maps = new ArrayList<>();
        for (Map map : credentials) {
            if (map.get("cloudPlatform").toString().equals(AZURE)) {
                maps.add(map);
                count++;
            }
        }
        return count == 0 ? false : true;
    }

    @CliCommand(value = "credential certificate", help = "get Azure certificate")
    public String getAzureCertificate(
            @CliOption(key = "id", mandatory = true, help = "id of the credential") String id
    ) {
        for (Map map : maps) {
            if (map.get("id").toString().equals(id)) {
                return cloudbreak.getCertificate(id);
            }
        }
        return "Azure certificate with this id does not exist";
    }
}
