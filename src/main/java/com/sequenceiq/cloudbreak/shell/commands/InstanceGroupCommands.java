package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderObjectValueMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Longs;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroup;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateId;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.InstanceGroupEntry;

import groovyx.net.http.HttpResponseException;

@Component
public class InstanceGroupCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "instancegroup configure")
    public boolean isCreateInstanceGroupAvailable() {
        return context.isBlueprintAvailable() && context.isCredentialAvailable();
    }


    @CliAvailabilityIndicator(value = "instancegroup show")
    public boolean isShowInstanceGroupAvailable() {
        return true;
    }

    @CliCommand(value = "instancegroup configure", help = "Configure instance groups")
    public String createInstanceGroup(
            @CliOption(key = "instanceGroup", mandatory = true, help = "Name of the instanceGroup") InstanceGroup instanceGroup,
            @CliOption(key = "nodecount", mandatory = true, help = "Nodecount for instanceGroup") Integer nodeCount,
            @CliOption(key = "templateId", mandatory = false, help = "TemplateId of the instanceGroup") InstanceGroupTemplateId instanceGroupTemplateId,
            @CliOption(key = "templateName", mandatory = false, help = "TemplateName of the instanceGroup") InstanceGroupTemplateName instanceGroupTemplateName)
            throws Exception {
        try {
            String templateId = null;
            if (instanceGroupTemplateId != null) {
                templateId = instanceGroupTemplateId.getName();
            } else if (instanceGroupTemplateName != null) {
                Object template = cloudbreak.getTemplateByName(instanceGroupTemplateName.getName());
                if (template != null) {
                    Map<String, Object> templateMap = (Map<String, Object>) template;
                    templateId = templateMap.get("id").toString();
                } else {
                    return String.format("Template not found by name: %s", instanceGroupTemplateName.getName());
                }
            } else {
                return "Template name or id is not defined for instanceGroup (use --templateName or --templateId)";
            }
            Long parsedTemplateId = Longs.tryParse(templateId);
            if (parsedTemplateId != null) {
                Map<Long, Integer> map = new HashMap<>();
                map.put(parsedTemplateId, nodeCount);
                if (!"cbgateway".equals(instanceGroup.getName())) {
                    context.putHostGroup(new HashMap.SimpleEntry<String, Object>(instanceGroup.getName(), new HashSet<>()));
                    context.putInstanceGroup(instanceGroup.getName(), new InstanceGroupEntry(parsedTemplateId, nodeCount, "CORE"));
                } else {
                    context.putInstanceGroup(instanceGroup.getName(), new InstanceGroupEntry(parsedTemplateId, nodeCount, "GATEWAY"));
                }
                if (context.getActiveHostGroups().size() == context.getInstanceGroups().size() - 1  && context.getActiveHostGroups().size() != 0) {
                    context.setHint(Hints.SELECT_NETWORK);
                } else {
                    context.setHint(Hints.CONFIGURE_HOSTGROUP);
                }
                return renderObjectValueMap(context.getInstanceGroups(), "instanceGroup");
            } else {
                return "TemplateId is not a number.";
            }
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @CliCommand(value = "instancegroup show", help = "Configure instance groups")
    public String showInstanceGroup() throws Exception {
        if (context.getInstanceGroups().isEmpty()) {
            return "List of instance groups is empty currently.";
        } else {
            return renderObjectValueMap(context.getInstanceGroups(), "instanceGroup");
        }
    }
}
