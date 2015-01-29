package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderObjectMapValueMap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.HostGroup;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateId;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

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
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") HostGroup hostgroup,
            @CliOption(key = "nodecount", mandatory = true, help = "Nodecount for hostgroup") Integer nodeCount,
            @CliOption(key = "templateId", mandatory = true, help = "TemplateId of the hostgroup") InstanceGroupTemplateId instanceGroupTemplateId)
            throws Exception {
        Map<Long, Integer> map = new HashMap<>();
        map.put(Long.parseLong(instanceGroupTemplateId.getName()), nodeCount);
        context.putInstanceGroup(hostgroup.getName(), map);
        if (context.getActiveHostgoups().size() == context.getInstanceGroups().size() && context.getActiveHostgoups().size() != 0) {
            context.setHint(Hints.CREATE_STACK);
        } else {
            context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
        }
        return renderObjectMapValueMap(context.getInstanceGroups(), "hostgroup", "templateId", "nodeCount");
    }

    @CliCommand(value = "instancegroup show", help = "Configure instance groups")
    public String showInstanceGroup() throws Exception {
        return renderObjectMapValueMap(context.getInstanceGroups(), "hostgroup", "templateId", "nodeCount");
    }
}
