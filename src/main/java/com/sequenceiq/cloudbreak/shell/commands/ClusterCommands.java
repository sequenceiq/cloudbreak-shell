package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;
import static java.lang.Integer.parseInt;

import java.util.ArrayList;
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
import com.sequenceiq.cloudbreak.shell.completion.HostGroup;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.StatusRequest;

import groovyx.net.http.HttpResponseException;

@Component
public class ClusterCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "cluster create")
    public boolean isClusterCreateCommandAvailable() {
        return context.isBlueprintAvailable() && context.isStackAvailable() && context.getActiveHostGroups().size() == context.getHostGroups().keySet().size();
    }

    @CliAvailabilityIndicator(value = { "cluster show", "cluster stop", "cluster start" })
    public boolean isClusterShowCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliAvailabilityIndicator({ "cluster node --ADD", "cluster node --REMOVE" })
    public boolean isClusterNodeCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliCommand(value = "cluster node --ADD", help = "Add new nodes to the cluster")
    public String addNodeToCluster(
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") HostGroup hostGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be added to the cluster") Integer adjustment) {
        try {
            if (adjustment < 1) {
                return "The adjustment value in case of node addition should be at least 1.";
            }
            cloudbreak.putCluster(Integer.valueOf(context.getStackId()), hostGroup.getName(), adjustment, false);
            return context.getStackId();
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster node --REMOVE", help = "Remove nodes from the cluster")
    public String removeNodeToCluster(
            @CliOption(key = "hostgroup", mandatory = true, help = "Name of the hostgroup") HostGroup hostGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "The number of the nodes to be removed from the cluster.") Integer adjustment,
            @CliOption(key = "withStackDownScale", mandatory = false, help = "Do the downscale with the stack together") Boolean withStackDownScale) {
        try {
            if (adjustment > -1) {
                return "The adjustment value in case of node removal should be negative.";
            }
            cloudbreak.putCluster(Integer.valueOf(context.getStackId()), hostGroup.getName(),
                    adjustment, withStackDownScale == null ? false : withStackDownScale);
            return context.getStackId();
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
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
            @CliOption(key = "userName", mandatory = false, unspecifiedDefaultValue = "admin", help = "Username of the Ambari server") String userName,
            @CliOption(key = "password", mandatory = false, unspecifiedDefaultValue = "admin", help = "Password of the Ambari server") String password,
            @CliOption(key = "description", mandatory = false, help = "Description of the blueprint") String description,
            @CliOption(key = "stack", mandatory = false, help = "Stack definition name, like HDP") String stack,
            @CliOption(key = "version", mandatory = false, help = "Stack definition version") String version,
            @CliOption(key = "os", mandatory = false, help = "Stack OS to select package manager, default is RedHat") String os,
            @CliOption(key = "stackRepoId", mandatory = false, help = "Stack repository id") String stackRepoId,
            @CliOption(key = "stackBaseURL", mandatory = false, help = "Stack url") String stackBaseURL,
            @CliOption(key = "utilsRepoId", mandatory = false, help = "Stack utils repoId") String utilsRepoId,
            @CliOption(key = "utilsBaseURL", mandatory = false, help = "Stack utils URL") String utilsBaseURL,
            @CliOption(key = "verify", mandatory = false, help = "Whether to verify the URLs or not") Boolean verify,
            @CliOption(key = "enableSecurity", mandatory = false, specifiedDefaultValue = "false", unspecifiedDefaultValue = "false",
                    help = "Kerberos security status") Boolean secure,
            @CliOption(key = "kerberosMasterKey", mandatory = false, specifiedDefaultValue = "key", unspecifiedDefaultValue = "key",
                    help = "Kerberos mater key") String kerberosMasterKey,
            @CliOption(key = "kerberosAdmin", mandatory = false, specifiedDefaultValue = "admin", unspecifiedDefaultValue = "admin",
                    help = "Kerberos admin name") String kerberosAdmin,
            @CliOption(key = "kerberosPassword", mandatory = false, specifiedDefaultValue = "admin", unspecifiedDefaultValue = "admin",
                    help = "Kerberos admin password") String kerberosPassword
    ) {
        try {
            List<Map<String, Object>> hostGroupList = new ArrayList<>();
            for (Map<String, Object> hostGroupListEntity : context.getHostGroups().values()) {
                Map<String, Object> hostGroupMap = new HashMap<String, Object>();
                hostGroupMap.put("name", hostGroupListEntity.keySet().iterator().next());
                hostGroupMap.put("instanceGroupName", hostGroupListEntity.keySet().iterator().next());
                hostGroupMap.put("recipeIds", hostGroupListEntity.values().iterator().next());
                hostGroupList.add(hostGroupMap);
            }
            cloudbreak.postCluster(
                    context.getStackName(),
                    userName, password,
                    parseInt(context.getBlueprintId()),
                    description,
                    parseInt(context.getStackId()),
                    hostGroupList,
                    stack, version, os,
                    stackRepoId, stackBaseURL,
                    utilsRepoId, utilsBaseURL,
                    verify,
                    secure, kerberosMasterKey, kerberosAdmin, kerberosPassword);
            context.setHint(Hints.NONE);
            return "Cluster creation started";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster stop", help = "Stop your cluster")
    public String stopCluster() {
        try {
            cloudbreak.putClusterStatus(Integer.valueOf(context.getStackId()), StatusRequest.STOPPED.name());
            return "Cluster is stopping";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "cluster start", help = "Start your cluster")
    public String startCluster() {
        try {
            cloudbreak.putClusterStatus(Integer.valueOf(context.getStackId()), StatusRequest.STARTED.name());
            return "Cluster is starting";
        } catch (Exception ex) {
            return ex.toString();
        }
    }
}
