package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.SecurityGroupId;
import com.sequenceiq.cloudbreak.shell.completion.SecurityGroupName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

import groovyx.net.http.HttpResponseException;

@Component
public class SecurityGroupCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreakClient;

    @CliAvailabilityIndicator({"security group list", "security group select", "security group show" })
    public boolean areSecurityGroupCommandsAvailable() {
        return true;
    }

    @CliCommand(value = "security group list", help = "Shows the currently available security groups")
    public String listNetworks() {
        try {
            Map<String, String> groups = cloudbreakClient.getAccountSecurityGroupsMap();
            Map<String, String> updatedGroups = new HashMap<>();
            for (String id : groups.keySet()) {
                updatedGroups.put(id, groups.get(id).toString());
            }
            context.setSecurityGroups(updatedGroups);
            return renderSingleMap(groups, "ID", "NAME");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @CliCommand(value = "security group select", help = "Select a security group by the given id or name")
    public String selectNetwork(
            @CliOption(key = "id", mandatory = false, help = "Id of the security group") SecurityGroupId secId,
            @CliOption(key = "name", mandatory = false, help = "Name of the security group") SecurityGroupName secName) {
        if (secId == null && secName == null) {
            return "Both ID and name cannot be null";
        }
        String message = "No security group has been selected";
        String id = secId == null ? null : secId.getName();
        String name = secName == null ? null : secName.getName();
        Map<String, String> securityGroups = context.getSecurityGroups();
        if (id != null && securityGroups.containsKey(id)) {
            context.setActiveSecurityGroupId(id);
            setHint();
            message = "Security group has been selected with id: " + id;
        } else if (securityGroups.containsValue(name)) {
            for (String groupId : securityGroups.keySet()) {
                if (securityGroups.get(groupId).equals(name)) {
                    context.setActiveSecurityGroupId(groupId);
                    setHint();
                    message = "Security group has been selected with name: " + name;
                    break;
                }
            }
        }
        return message;
    }

    @CliCommand(value = "security group show", help = "Shows the security group by its id or name")
    public Object showNetwork(
            @CliOption(key = "id", mandatory = false, help = "Id of the network") SecurityGroupId groupId,
            @CliOption(key = "name", mandatory = false, help = "Name of the network") SecurityGroupName groupName) {
        try {
            String id = groupId == null ? null : groupId.getName();
            String name = groupName == null ? null : groupName.getName();
            if (id != null) {
                Map<String, Object> fields = (Map<String, Object>) cloudbreakClient.getSecurityGroup(id);
                return renderSingleMap(getNetworkFieldsAsSingleStringMap(fields), "FIELD", "VALUE");
            } else if (name != null) {
                Map<String, Object> networkFields = (Map<String, Object>) cloudbreakClient.getSecurityGroupByName(name);
                return renderSingleMap(getNetworkFieldsAsSingleStringMap(networkFields), "FIELD", "VALUE");
            }
            return "Security group could not be found!";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private Map<String, String> getNetworkFieldsAsSingleStringMap(Map<String, Object> fields) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> networkField : fields.entrySet()) {
            result.put(networkField.getKey(), networkField.getValue().toString());
        }
        return result;
    }

    private void setHint() {
        context.setHint(Hints.CREATE_STACK);
    }

}
