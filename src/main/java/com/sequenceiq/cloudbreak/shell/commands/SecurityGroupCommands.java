package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.util.HashMap;
import java.util.List;
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
import com.sequenceiq.cloudbreak.shell.completion.SecurityRules;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

import groovyx.net.http.HttpResponseException;

@Component
public class SecurityGroupCommands implements CommandMarker {
    private static final String CREATE_SUCCESS_MSG = "Security group created and selected successfully, with id: '%s'";

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreakClient;

    @CliAvailabilityIndicator({ "securitygroup create", "securitygroup list" })
    public boolean areSecurityGroupCommandsAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({ "securitygroup delete", "securitygroup select", "securitygroup show" })
    public boolean areExistSecurityGroupCommandsAvailable() {
        return !context.getSecurityGroups().isEmpty();
    }

    @CliCommand(value = "securitygroup create", help = "Creates a new security group")
    public String createSecurityGroup(
        @CliOption(key = "name", mandatory = true, help = "Name of the security group") String name,
        @CliOption(key = "description", mandatory = false, help = "Description of the security group") String description,
        @CliOption(key = "rules", mandatory = true,
                help = "Security rules in the following format: ';' separated list of <cidr>:<protocol>:<comma separated port list>") SecurityRules rules,
        @CliOption(key = "publicInAccount", mandatory = false, help = "Marks the securitygroup as visible for all members of the account",
                specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") Boolean publicInAccount) throws Exception {
        try {
            Map<String, String> tcpRules = new HashMap<>();
            Map<String, String> udpRules = new HashMap<>();
            for (Map<String, String> rule : rules.getRules()) {
                if ("tcp".equals(rule.get("protocol"))) {
                    tcpRules.put(rule.get("subnet"), rule.get("ports"));
                } else {
                    udpRules.put(rule.get("subnet"), rule.get("ports"));
                }
            }
            String id = cloudbreakClient.postSecurityGroup(name, description, tcpRules, udpRules, publicInAccount);
            context.putSecurityGroup(id, name);
            context.setActiveSecurityGroupId(id);
            setHint();
            return String.format(CREATE_SUCCESS_MSG, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "securitygroup list", help = "Shows the currently available security groups")
    public String listSecurityGroups() {
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

    @CliCommand(value = "securitygroup select", help = "Select a security group by the given id or name")
    public String selectSecurityGroup(
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

    @CliCommand(value = "securitygroup show", help = "Shows the security group by its id or name")
    public Object showSecurityGroup(
            @CliOption(key = "id", mandatory = false, help = "Id of the security group") SecurityGroupId groupId,
            @CliOption(key = "name", mandatory = false, help = "Name of the security group") SecurityGroupName groupName) {
        try {
            String id = groupId == null ? null : groupId.getName();
            String name = groupName == null ? null : groupName.getName();
            if (id != null) {
                Map<String, Object> fields = (Map<String, Object>) cloudbreakClient.getSecurityGroup(id);
                return renderSingleMap(getSecurityGroupFieldsAsSingleStringMap(fields), "FIELD", "VALUE");
            } else if (name != null) {
                Map<String, Object> securityGroupFields = (Map<String, Object>) cloudbreakClient.getSecurityGroupByName(name);
                return renderSingleMap(getSecurityGroupFieldsAsSingleStringMap(securityGroupFields), "FIELD", "VALUE");
            }
            return "Security group could not be found!";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @CliCommand(value = "securitygroup delete", help = "Delete security group by the given id or name")
    public Object deleteSecurityGroup(
            @CliOption(key = "id", mandatory = false, help = "Id of the security group") SecurityGroupId securityGroupId,
            @CliOption(key = "name", mandatory = false, help = "Name of the security group") SecurityGroupName securityGroupName) {
        try {
            String id = securityGroupId == null ? null : securityGroupId.getName();
            String name = securityGroupName == null ? null :  securityGroupName.getName();
            if (id != null) {
                Object result = cloudbreakClient.deleteSecurityGroup(id);
                refreshSecurityGroupsInContext();
                return result;
            } else if (name != null) {
                Object result = cloudbreakClient.deleteSecurityGroupByName(name);
                refreshSecurityGroupsInContext();
                return result;
            }
            return "No security group specified.";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private Map<String, String> getSecurityGroupFieldsAsSingleStringMap(Map<String, Object> fields) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> securityGroupField : fields.entrySet()) {
            if (securityGroupField.getValue() != null) {
                result.put(securityGroupField.getKey(), securityGroupField.getValue().toString());
            }
        }
        return result;
    }

    private void refreshSecurityGroupsInContext() throws Exception {
        context.getSecurityGroups().clear();
        List<Map> accountSecurityGroups = cloudbreakClient.getAccountSecurityGroups();
        for (Map securityGroup : accountSecurityGroups) {
            context.putSecurityGroup(securityGroup.get("id").toString(), securityGroup.get("name").toString());
        }
        if (!context.getSecurityGroups().containsKey(context.getActiveSecurityGroupId())) {
            context.setActiveSecurityGroupId(null);
        }
    }

    private void setHint() {
        context.setHint(Hints.CREATE_STACK);
    }

}
