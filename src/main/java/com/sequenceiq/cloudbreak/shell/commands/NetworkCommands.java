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
import com.sequenceiq.cloudbreak.shell.completion.NetworkId;
import com.sequenceiq.cloudbreak.shell.completion.NetworkName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

import groovyx.net.http.HttpResponseException;

@Component
public class NetworkCommands implements CommandMarker {
    private static final String CREATE_SUCCESS_MSG = "Network created and selected successfully, with id: '%s'";

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreakClient;

    @CliAvailabilityIndicator({"network list", "network create --AWS", "network create --AZURE", "network create --GCP", "network create --OPENSTACK" })
    public boolean areNetworkCommandsAvailable() {
        return true;
    }

    @CliAvailabilityIndicator({"network delete", "network select", "network show" })
    public boolean isNetworkDeleteCommandAvailable() {
        return !context.getNetworksByProvider().isEmpty();
    }

    @CliCommand(value = "network list", help = "Shows the currently available networks configurations")
    public String listNetworks() {
        try {
            Map<String, String> networksMap = cloudbreakClient.getAccountNetworksMap();
            return renderSingleMap(networksMap, "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @CliCommand(value = "network create --AWS", help = "Create a new AWS network configuration")
    public String createAwsNetwork(
            @CliOption(key = "name", mandatory = true, help = "Name of the network") String name,
            @CliOption(key = "subnet", mandatory = true, help = "Subnet of the network in CIDR format") String subnet,
            @CliOption(key = "vpcID", mandatory = false, help = "The ID of the virtual private cloud (VPC)") String vpcId,
            @CliOption(key = "internetGatewayID", mandatory = false,
                    help = "The ID of the internet gateway that is attached to the VPC (configured via 'vpcID' option)") String internetGatewayId,
            @CliOption(key = "publicInAccount", mandatory = false, help = "Marks the network as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the network") String description
    ) {
        try {
            String id = cloudbreakClient.postAWSNetwork(name, description, subnet, vpcId, internetGatewayId, publicInAccount == null ? false : publicInAccount);
            createHintAndAddNetworkToContext(id, "AWS");
            return String.format(CREATE_SUCCESS_MSG, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network create --AZURE", help = "Create a new AZURE network configuration")
    public String createAzureNetwork(
            @CliOption(key = "name", mandatory = true, help = "Name of the network") String name,
            @CliOption(key = "addressPrefix", mandatory = true, help = "The address prefix of the Azure virtual network in CIDR format") String addressPrefix,
            @CliOption(key = "subnet", mandatory = true, help = "Subnet of the network in CIDR format") String subnet,
            @CliOption(key = "publicInAccount", mandatory = false, help = "Marks the network as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the network") String description
    ) {
        try {
            String id = cloudbreakClient.postAzureNetwork(name, description, subnet, addressPrefix, publicInAccount == null ? false : publicInAccount);
            createHintAndAddNetworkToContext(id, "AZURE");
            return String.format(CREATE_SUCCESS_MSG, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network create --GCP", help = "Create a new GCP network configuration")
    public String createGcpNetwork(
            @CliOption(key = "name", mandatory = true, help = "Name of the network") String name,
            @CliOption(key = "subnet", mandatory = true, help = "Subnet of the network in CIDR format") String subnet,
            @CliOption(key = "publicInAccount", mandatory = false, help = "Marks the network as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the network") String description
    ) {
        try {
            String id = cloudbreakClient.postGCPNetwork(name, description, subnet, publicInAccount == null ? false : publicInAccount);
            createHintAndAddNetworkToContext(id, "GCP");
            return String.format(CREATE_SUCCESS_MSG, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network create --OPENSTACK", help = "Create a new OpenStack network configuration")
    public String createOpenStackNetwork(
            @CliOption(key = "name", mandatory = true, help = "Name of the network") String name,
            @CliOption(key = "subnet", mandatory = true, help = "Subnet of the network in CIDR format") String subnet,
            @CliOption(key = "publicNetID", mandatory = true, help = "ID of the available and desired OpenStack public network") String publicNetID,
            @CliOption(key = "publicInAccount", mandatory = false, help = "Marks the network as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the network") String description
    ) {
        try {
            String id = cloudbreakClient.postOpenStackNetwork(name, description, subnet, publicNetID, publicInAccount == null ? false : publicInAccount);
            createHintAndAddNetworkToContext(id, "OPENSTACK");
            return String.format(CREATE_SUCCESS_MSG, id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network select", help = "Select network by the given id or name")
    public String selectNetwork(
            @CliOption(key = "id", mandatory = false, help = "Id of the network") NetworkId idOfNetwork,
            @CliOption(key = "name", mandatory = false, help = "Name of the network") NetworkName networkName) {
        try {
            String msg = "Network could not be found.";
            String id = idOfNetwork == null ? null : idOfNetwork.getName();
            String name = networkName == null ? null :  networkName.getName();
            if (id != null && context.getNetworksByProvider().containsKey(id)) {
                String provider = context.getNetworksByProvider().get(id);
                createHintAndAddNetworkToContext(id, provider);
                msg = "Network is selected with id: " + id;
            } else if (name != null) {
                Map<String, Object> network = (Map<String, Object>) cloudbreakClient.getNetworkByName(name);
                if (network != null && !network.isEmpty()) {
                    String networkId = (String) network.get("id");
                    createHintAndAddNetworkToContext(networkId, name);
                    msg = "Network is selected with name: " + name;
                }
            }
            return msg;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network delete", help = "Delete network by the given id or name")
    public Object deleteNetwork(
            @CliOption(key = "id", mandatory = false, help = "Id of the network") NetworkId networkId,
            @CliOption(key = "name", mandatory = false, help = "Name of the network") NetworkName networkName) {
        try {
            String id = networkId == null ? null : networkId.getName();
            String name = networkName == null ? null :  networkName.getName();
            if (id != null) {
                Object result = cloudbreakClient.deleteNetwork(id);
                refreshNetworksInContext();
                return result;
            } else if (name != null) {
                Object result = cloudbreakClient.deleteNetworkByName(name);
                refreshNetworksInContext();
                return result;
            }
            return "No network specified.";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "network show", help = "Shows the network by its id or name")
    public Object showNetwork(
            @CliOption(key = "id", mandatory = false, help = "Id of the network") NetworkId networkId,
            @CliOption(key = "name", mandatory = false, help = "Name of the network") NetworkName networkName) {
        try {
            String id = networkId == null ? null : networkId.getName();
            String name = networkName == null ? null :  networkName.getName();
            if (id != null) {
                Map<String, Object> networkFields = (Map<String, Object>) cloudbreakClient.getNetwork(id);
                return renderSingleMap(getNetworkFieldsAsSingleStringMap(networkFields), "FIELD", "VALUE");
            } else if (name != null) {
                Map<String, Object> networkFields = (Map<String, Object>) cloudbreakClient.getNetworkByName(name);
                return renderSingleMap(getNetworkFieldsAsSingleStringMap(networkFields), "FIELD", "VALUE");
            }
            return "Network could not be found!";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private Map<String, String> getNetworkFieldsAsSingleStringMap(Map<String, Object> networkFields) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> networkField : networkFields.entrySet()) {
            if ("parameters".equals(networkField.getKey())) {
                for (Map.Entry<String, Object> parameter : ((Map<String, Object>) networkField.getValue()).entrySet()) {
                    if (parameter.getValue() != null) {
                        result.put(parameter.getKey(), parameter.getValue().toString());
                    }
                }
            } else {
                result.put(networkField.getKey(), networkField.getValue().toString());
            }
        }
        return result;
    }

    private void createHintAndAddNetworkToContext(String id, String provider) throws Exception {
        context.setHint(Hints.SELECT_SECURITY_GROUP);
        context.putNetwork(id, provider);
        context.setActiveNetworkId(id);
    }

    private void refreshNetworksInContext() throws Exception {
        context.getNetworksByProvider().clear();
        List<Map> accountNetworks = cloudbreakClient.getAccountNetworks();
        for (Map network : accountNetworks) {
            context.putNetwork(network.get("id").toString(), network.get("cloudPlatform").toString());
        }
        if (!context.getNetworksByProvider().containsKey(context.getActiveNetworkId())) {
            context.setActiveNetworkId(null);
        }
    }
}
