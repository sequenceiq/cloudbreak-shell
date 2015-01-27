package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.StackRegion;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

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
        return context.isCredentialAvailable() && (context.getActiveHostgoups().size() == context.getInstanceGroups().size());
    }

    @CliAvailabilityIndicator(value = "stack show")
    public boolean isStackShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack select")
    public boolean isStackSelectCommandAvailable() throws Exception {
        return context.isStackAccessible();
    }

    @CliCommand(value = "stack create", help = "Create a new stack based on a template")
    public String createStack(
            @CliOption(key = "name", mandatory = true, help = "Name of the stack") String name,
            @CliOption(key = "userName", mandatory = true, specifiedDefaultValue = "admin", help = "Username of the Ambari server") String userName,
            @CliOption(key = "region", mandatory = true, help = "region of the stack") StackRegion region,
            @CliOption(key = "password", mandatory = true, specifiedDefaultValue = "admin", help = "Password of the Ambari server") String password,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the stack is public in the account")
            Boolean publicInAccount) {
        try {
            if (publicInAccount == null) {
                publicInAccount = false;
            }
            String id =
                    cloudbreak.postStack(name, userName, password, context.getCredentialId(), region.getName(), publicInAccount, context.getInstanceGroups());
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
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        try {
            Object stack = cloudbreak.getStack(id);
            if (stack != null) {
                context.addStack(id, ((HashMap) stack).get("name").toString());
                context.setHint(Hints.CREATE_CLUSTER);
                return "Stack selected, id: " + id;
            } else {
                return "No stack specified";
            }
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack terminate", help = "Terminate the stack by its id")
    public String terminateStack(
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        try {
            cloudbreak.terminateStack(id);
            context.setHint(Hints.CREATE_CLUSTER);
            context.removeStack(id);
            return "Stack terminated with id:" + id;
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

    @CliCommand(value = "stack show", help = "Shows the stack by its id")
    public Object showStack(
            @CliOption(key = "id", mandatory = true, help = "Id of the stack") String id) {
        try {
            Map<String, String> stackMap = cloudbreak.getStackMap(id);
            stackMap.remove("description");
            return renderSingleMap(stackMap, "FIELD", "VALUE");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }


}
