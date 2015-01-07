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
        return context.isBlueprintAvailable() || context.isRecipeAvailable() && context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "cluster show")
    public boolean isClusterShowCommandAvailable() {
        return true;
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

    @CliCommand(value = "cluster create", help = "Create a new cluster based on a blueprint and template")
    public String createCluster(
            @CliOption(key = "description", mandatory = false, help = "Description of the blueprint") String description,
            @CliOption(key = "fromRecipe", mandatory = false, unspecifiedDefaultValue = "false",
                    help = "True if the cluster should be created from a recipe instead of a blueprint") boolean fromRecipe) {
        try {
            if (!fromRecipe) {
                if (!context.isBlueprintAvailable()) {
                    return "No blueprint in context, cannot create cluster!";
                }
                cloudbreak.postCluster(context.getStackName(), parseInt(context.getBlueprintId()), null, description, parseInt(context.getStackId()));
            } else {
                if (!context.isRecipeAvailable()) {
                    return "No recipe in context, cannot create cluster!";
                }
                cloudbreak.postCluster(context.getStackName(), null, parseInt(context.getRecipeId()), description, parseInt(context.getStackId()));
            }
            context.setHint(Hints.NONE);
            return "Cluster created";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

}
