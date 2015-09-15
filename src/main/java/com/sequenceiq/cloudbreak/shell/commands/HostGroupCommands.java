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
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.HostGroup;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;

import groovyx.net.http.HttpResponseException;

@Component
public class HostGroupCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "hostgroup configure")
    public boolean isCreateHostGroupAvailable() {
        return context.isBlueprintAvailable() && context.isCredentialAvailable() || context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "hostgroup show")
    public boolean isShowHostGroupAvailable() {
        return true;
    }

    @CliCommand(value = "hostgroup configure", help = "Configure host groups")
    public String createHostGroup(
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") HostGroup hostgroup,
            @CliOption(key = "recipeIds", mandatory = false, help = "A comma separated list of recipe ids") String recipeIds,
            @CliOption(key = "recipeNames", mandatory = false, help = "A comma separated list of recipe names") String recipeNames)
            throws Exception {
        try {
            Set<Long> recipeIdSet = new HashSet<>();
            if (recipeIds != null) {
                recipeIdSet.addAll(getRecipeIds(recipeIds, RecipeParameterType.ID));
            }
            if (recipeNames != null) {
                recipeIdSet.addAll(getRecipeIds(recipeNames, RecipeParameterType.NAME));
            }
            Map.Entry<String, Object> hostGroupMapEntry = new HashMap.SimpleEntry<String, Object>(hostgroup.getName(), recipeIdSet);
            context.putHostGroup(hostGroupMapEntry);
            return renderObjectMapValueMap(context.getHostGroups(), "hostgroup", "instanceGroupName", "recipeIds");
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @CliCommand(value = "hostgroup show", help = "Configure host groups")
    public String showHostGroup() throws Exception {
        return renderObjectMapValueMap(context.getHostGroups(), "hostgroup", "instanceGroupName", "recipeIds");
    }

    private enum RecipeParameterType {
        ID, NAME
    }

    private Set<Long> getRecipeIds(String inputs, final RecipeParameterType type) {
        return FluentIterable.from(Splitter.on(",").omitEmptyStrings().trimResults().split(inputs)).transform(new Function<String, Long>() {
            @Override
            public Long apply(String input) {
                try {
                    Map<String, Object> resp = null;
                    switch (type) {
                        case ID:
                            resp = (Map) cloudbreak.getRecipe(input);
                            break;
                        case NAME:
                            resp = (Map) cloudbreak.getRecipeByName(input);
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                    return Long.parseLong(resp.get("id").toString());
                } catch (HttpResponseException e) {
                    throw new RuntimeException("Recipe [" + input + "]: " + e.getResponse().getData().toString());
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }).toSet();
    }
}
