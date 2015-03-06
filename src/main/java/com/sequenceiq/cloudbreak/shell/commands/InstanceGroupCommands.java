package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderObjectMapValueMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.primitives.Longs;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.HostGroup;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateId;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

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
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") HostGroup hostgroup,
            @CliOption(key = "nodecount", mandatory = true, help = "Nodecount for hostgroup") Integer nodeCount,
            @CliOption(key = "templateId", mandatory = false, help = "TemplateId of the hostgroup") InstanceGroupTemplateId instanceGroupTemplateId,
            @CliOption(key = "templateName", mandatory = false, help = "TemplateName of the hostgroup") InstanceGroupTemplateName instanceGroupTemplateName,
            @CliOption(key = "recipeIds", mandatory = false, help = "A comma separated list of recipe ids") String recipeIds)
            throws Exception {
        try {
            String templateId = null;
            Set<Long> recipeIdSet = new HashSet<>();
            if (recipeIds != null) {
                recipeIdSet = FluentIterable.from(Splitter.on(",").omitEmptyStrings().trimResults().split(recipeIds)).transform(new Function<String, Long>() {
                    @Override
                    public Long apply(String input) {
                        try {
                            cloudbreak.getRecipe(input);
                        } catch (HttpResponseException e) {
                            throw new RuntimeException("Recipe [" + input + "]: " + e.getResponse().getData().toString());
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                        return Long.parseLong(input);
                    }
                }).toSet();
            }
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
                return "Template name or id is not defined for host group (use --templateName or --templateId)";
            }
            Long parsedTemplateId = Longs.tryParse(templateId);
            if (parsedTemplateId != null) {
                Map<Long, Integer> map = new HashMap<>();
                map.put(parsedTemplateId, nodeCount);
                context.putInstanceGroup(hostgroup.getName(), map);


                Map<String, Object> existingConfig = context.getHostGroupByName(hostgroup.getName());
                if (existingConfig != null) {
                    context.getHostGroups().remove(existingConfig);
                }
                Map<String, Object> hostGroupMap = new HashMap<String, Object>();
                hostGroupMap.put("name", hostgroup.getName());
                hostGroupMap.put("instanceGroupName", hostgroup.getName());
                hostGroupMap.put("recipeIds", recipeIdSet);
                context.putHostGroup(hostGroupMap);

                if (context.getActiveHostgoups().size() == context.getInstanceGroups().size() && context.getActiveHostgoups().size() != 0) {
                    context.setHint(Hints.CREATE_STACK);
                } else {
                    context.setHint(Hints.CONFIGURE_INSTANCEGROUP);
                }
                return renderObjectMapValueMap(context.getInstanceGroups(), "hostgroup", "templateId", "nodeCount");
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
        return renderObjectMapValueMap(context.getInstanceGroups(), "hostgroup", "templateId", "nodeCount");
    }
}
