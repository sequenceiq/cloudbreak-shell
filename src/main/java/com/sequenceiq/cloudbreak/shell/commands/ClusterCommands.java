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
import static java.lang.Integer.parseInt;

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
public class ClusterCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "cluster create")
    public boolean isClusterCreateCommandAvailable() {
        return context.isBlueprintAvailable() && context.isCredentialAvailable() && context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "cluster show")
    public boolean isClusterShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "cluster list")
    public boolean isClusterListCommandAvailable() {
        return true;
    }

    @CliCommand(value = "cluster show", help = "Shows the cluster by its id")
    public Object showCluster(
            @CliOption(key = "id", mandatory = true, help = "Id of the cluster") String id) {
        return cloudbreak.getCluster(id);
    }

    @CliCommand(value = "cluster list", help = "Shows all of your clusters")
    public String listClusters() {
        return renderSingleMap(cloudbreak.getClustersMap(), "ID", "INFO");
    }


    @CliCommand(value = "cluster create", help = "Create a new cluster based on a blueprint and template")
    public String createCluster(
            @CliOption(key = "name", mandatory = true, help = "Name of the cluster") String name,
            @CliOption(key = "description", mandatory = true, help = "Description of the blueprint") String description,
            @CliOption(key = "blueprintId", mandatory = true, help = "Id of the blueprint") String blueprint) {
        cloudbreak.postCluster(name, parseInt(blueprint), description, parseInt(context.getStackId()));
        context.setHint(Hints.NONE);
        return "Cluster created";
    }

}
