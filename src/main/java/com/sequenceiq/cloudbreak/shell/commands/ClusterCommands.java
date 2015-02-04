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

import groovyx.net.http.HttpResponseException;

@Component
public class ClusterCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "cluster create")
    public boolean isClusterCreateCommandAvailable() {
        return context.isBlueprintAvailable() && context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "cluster show")
    public boolean isClusterShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "cluster node --ADD")
    public boolean isClusterAddNodeCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "cluster node --REMOVE")
    public boolean isClusterRemoveNodeCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliCommand(value = "cluster node --ADD", help = "Add new nodes to the cluster")
    public String addNodeToCluster(
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") String hostGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be added to the cluster") Integer adjustment) {
        try {
            cloudbreak.putCluster(Integer.valueOf(context.getStackId()), hostGroup, adjustment);
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster node --REMOVE", help = "Remove nodes to the cluster")
    public String removeNodeToCluster(
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") String hostGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be removed to the cluster") Integer adjustment) {
        try {
            cloudbreak.putCluster(Integer.valueOf(context.getStackId()), hostGroup, adjustment * (-1));
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster show", help = "Shows the cluster by stack id")
    public Object showCluster() {
        try {
            return renderSingleMap(cloudbreak.getClusterMap(context.getStackId()), "FIELD", "VALUE");
        } catch (IndexOutOfBoundsException ex) {
            return "There was no cluster for this account.";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster create", help = "Create a new cluster based on a blueprint and optionally a recipe")
    public String createCluster(
            @CliOption(key = "description", mandatory = false, help = "Description of the blueprint") String description) {
        try {
            Integer recipeId = null;
            if (context.isRecipeAvailable()) {
                recipeId = parseInt(context.getRecipeId());
            }
            cloudbreak.postCluster(context.getStackName(), parseInt(context.getBlueprintId()), recipeId, description, parseInt(context.getStackId()));
            context.setHint(Hints.NONE);
            return "Cluster creation started";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

}
