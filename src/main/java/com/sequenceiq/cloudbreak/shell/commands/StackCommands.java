package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroup;
import com.sequenceiq.cloudbreak.shell.completion.PlatformVariant;
import com.sequenceiq.cloudbreak.shell.completion.StackAvailabilityZone;
import com.sequenceiq.cloudbreak.shell.completion.StackRegion;
import com.sequenceiq.cloudbreak.shell.model.AdjustmentType;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.OnFailureAction;
import com.sequenceiq.cloudbreak.shell.model.StatusRequest;
import com.sequenceiq.cloudbreak.shell.util.MessageUtil;

import groovyx.net.http.HttpResponseException;

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
        return context.isCredentialAvailable()
                && (context.getActiveHostGroups().size() == context.getInstanceGroups().size() - 1
                && context.getActiveHostGroups().size() != 0);
    }

    @CliAvailabilityIndicator({ "stack node --ADD", "stack node --REMOVE", "stack stop", "stack start" })
    public boolean isStackNodeCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "stack show")
    public boolean isStackShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack select")
    public boolean isStackSelectCommandAvailable() throws Exception {
        return context.isStackAccessible();
    }

    @CliCommand(value = "stack node --ADD", help = "Add new nodes to the cluster")
    public String addNodeToStack(
            @CliOption(key = "instanceGroup", mandatory = true, help = "Name of the instanceGroup") InstanceGroup instanceGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be added to the stack") Integer adjustment,
            @CliOption(key = "withClusterUpScale", mandatory = false, help = "Do the upscale with the cluster together") Boolean withClusterUpScale) {
        try {
            if (adjustment < 1) {
                return "The adjustment value in case of node addition should be at least 1.";
            }
            cloudbreak.putStack(Integer.valueOf(context.getStackId()), instanceGroup.getName(), adjustment,
                    withClusterUpScale == null ? false : withClusterUpScale);
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack node --REMOVE", help = "Remove nodes from the cluster")
    public String removeNodeToStack(
            @CliOption(key = "instanceGroup", mandatory = true, help = "Name of the instanceGroup") InstanceGroup instanceGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be removed to the stack") Integer adjustment) {
        try {
            if (adjustment > -1) {
                return "The adjustment value in case of node removal should be negative.";
            }
            cloudbreak.putStack(Integer.valueOf(context.getStackId()), instanceGroup.getName(), adjustment, false);
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack create", help = "Create a new stack based on a template")
    public String createStack(
            @CliOption(key = "name", mandatory = true, help = "Name of the stack") String name,
            @CliOption(key = "region", mandatory = true, help = "region of the stack") StackRegion region,
            @CliOption(key = "availabilityZone", mandatory = false, help = "availabilityZone of the stack") StackAvailabilityZone availabilityZone,
            @CliOption(key = "image", mandatory = false, specifiedDefaultValue = "image-name", help = "Specific image name") String image,
            @CliOption(key = "publicInAccount", mandatory = false, help = "marks the stack as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "onFailureAction", mandatory = false, help = "onFailureAction which is ROLLBACK or DO_NOTHING.") OnFailureAction onFailureAction,
            @CliOption(key = "adjustmentType", mandatory = false, help = "adjustmentType which is EXACT or PERCENTAGE.") AdjustmentType adjustmentType,
            @CliOption(key = "threshold", mandatory = false, help = "threshold of failure") Long threshold,
            @CliOption(key = "diskPerStorage", mandatory = false, help = "disk per Storage Account on Azure") Integer diskPerStorage,
            @CliOption(key = "platformVariant", mandatory = false, help = "select platform variant version") PlatformVariant platformVariant,
            @CliOption(key = "dedicatedInstances", mandatory = false, help = "request dedicated instances on AWS") Boolean dedicatedInstances) {
        try {
            String networkId = context.getActiveNetworkId();
            if (networkId == null || (networkId != null && context.getNetworksByProvider().get(networkId) == context.getActiveCloudPlatform())) {
                return "A network must be selected with the same cloud platform as the credential!";
            }
            String securityGroupId = context.getActiveSecurityGroupId();
            if (securityGroupId == null) {
                return "A security group must be selected";
            }
            Collection<String> zonesByRegion = context.getAvailabilityZonesByRegion(context.getActiveCloudPlatform(), region.getName());
            if (availabilityZone != null) {
                if (zonesByRegion != null && !zonesByRegion.contains(availabilityZone.getName())) {
                    return "Availability zone is not in the selected region. The available zones in the regions are: "
                            + zonesByRegion;
                }
            } else if ("GCP".equals(context.getActiveCloudPlatform())) {
                if (zonesByRegion != null && zonesByRegion.size() > 0) {
                    availabilityZone = new StackAvailabilityZone(zonesByRegion.iterator().next());
                }
            }
            String id =
                    cloudbreak.postStack(
                            name,
                            context.getCredentialId(),
                            region.getName(),
                            publicInAccount == null ? false : publicInAccount,
                            context.getInstanceGroups(),
                            onFailureAction == null ? OnFailureAction.ROLLBACK.name() : onFailureAction.name(),
                            threshold == null ? 1L : threshold,
                            adjustmentType == null ? AdjustmentType.BEST_EFFORT.name() : adjustmentType.name(),
                            image,
                            networkId,
                            securityGroupId,
                            diskPerStorage,
                            dedicatedInstances,
                            platformVariant == null ? "" : platformVariant.getName(),
                            availabilityZone == null ? null : availabilityZone.getName());
            context.addStack(id, name);
            context.setHint(Hints.CREATE_CLUSTER);
            return "Stack created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack select", help = "Select the stack by its id")
    public String selectStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            if (id != null) {
                Object stack = cloudbreak.getStack(id);
                if (stack != null) {
                    context.addStack(id, ((Map) stack).get("name").toString());
                    if (context.isCredentialAvailable()) {
                        context.setHint(Hints.CREATE_CLUSTER);
                    } else {
                        context.setHint(Hints.CONFIGURE_HOSTGROUP);
                    }
                    prepareCluster(id);
                    context.prepareInstanceGroups(stack);
                    return "Stack selected, id: " + id;
                }

            } else if (name != null) {
                Object stack = cloudbreak.getStackByName(name);
                if (stack != null) {
                    String stackId = ((Map) stack).get("id").toString();
                    context.addStack(stackId, name);
                    if (context.isCredentialAvailable()) {
                        context.setHint(Hints.CREATE_CLUSTER);
                    } else {
                        context.setHint(Hints.CONFIGURE_HOSTGROUP);
                    }
                    prepareCluster(stackId);
                    context.prepareInstanceGroups(stack);
                    return "Stack selected, name: " + name;
                }
            }
            return "No stack specified. (select by using --id or --name)";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private void prepareCluster(String stackId) {
        try {
            Object cluster = cloudbreak.getCluster(stackId);
            if (cluster != null) {
                String blueprintId = ((Map) cluster).get("blueprintId").toString();
                context.addBlueprint(blueprintId);
            }
        } catch (Exception e) {
            return;
        }
    }

    @CliCommand(value = "stack terminate", help = "Terminate the stack by its id")
    public String terminateStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            if (id != null) {
                cloudbreak.terminateStack(id);
                context.setHint(Hints.CREATE_CLUSTER);
                context.removeStack(id);
                return "Stack terminated with id: " + id;
            } else if (name != null) {
                Object stack = cloudbreak.getStackByName(name);
                if (stack != null) {
                    Map<String, Object> sMap = (Map<String, Object>) stack;
                    String stackId = sMap.get("id").toString();
                    cloudbreak.terminateStack(stackId);
                    context.setHint(Hints.CREATE_CLUSTER);
                    context.removeStack(stackId);
                    return "Stack terminated with name: " + name;
                }
            }
            return "Stack not specified. (select by using --id or --name)";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack list", help = "Shows all of your stack")
    public String listStacks() {
        try {
            return renderSingleMap(cloudbreak.getAccountStacksMap(), "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack stop", help = "Stop your stack")
    public String stopStack() {
        try {
            cloudbreak.putStackStatus(Integer.valueOf(context.getStackId()), StatusRequest.STOPPED.name());
            return "Stack is stopping";
        } catch (Exception ex) {
            return MessageUtil.getMessage(ex);
        }
    }

    @CliCommand(value = "stack start", help = "Start your stack")
    public String startStack() {
        try {
            cloudbreak.putStackStatus(Integer.valueOf(context.getStackId()), StatusRequest.STARTED.name());
            return "Stack is starting";
        } catch (Exception ex) {
            return MessageUtil.getMessage(ex);
        }
    }


    @CliCommand(value = "stack show", help = "Shows the stack by its id")
    public Object showStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            if (id != null) {
                Map<String, String> stackMap = cloudbreak.getStackMap(id);
                stackMap.remove("description");
                return renderSingleMap(stackMap, "FIELD", "VALUE");
            } else if (name != null) {
                Object stack = cloudbreak.getStackByName(name);
                if (stack != null) {
                    Map<String, Object> sMap = (Map<String, Object>) stack;
                    String stackId = sMap.get("id").toString();
                    Map<String, String> stackMap = cloudbreak.getStackMap(stackId);
                    stackMap.remove("description");
                    return renderSingleMap(stackMap, "FIELD", "VALUE");
                }
            }
            return "No stack specified.";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }


}
