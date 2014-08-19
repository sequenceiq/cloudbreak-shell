package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

import groovyx.net.http.HttpResponseException;

@Component
public class CredentialCommands implements CommandMarker {

    public static final String AZURE = "AZURE";
    private List<Map> maps = new ArrayList<>();

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "credential list")
    public boolean isCredentialListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential delete")
    public boolean isCredentialDeleteCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential show")
    public boolean isCredentialShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential select")
    public boolean isCredentialSelectCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "credential createEC2")
    public boolean isCredentialEc2CreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential show", help = "Shows the credential by its id")
    public Object showCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        try {
            return renderSingleMap(cloudbreak.getCredentialMap(id), "FIELD", "VALUE");
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential delete", help = "Delete the credential by its id")
    public Object deleteCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        try {
            return cloudbreak.deleteCredential(id);
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential list", help = "Shows all of your credentials")
    public String listCredentials() {
        try {
            return renderSingleMap(cloudbreak.getCredentialsMap(), "ID", "INFO");
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential select", help = "Select the credential by its id")
    public String selectCredential(
            @CliOption(key = "id", mandatory = true, help = "Id of the credential") String id) {
        try {

            if (cloudbreak.getCredential(id) != null) {
                context.setCredential(id);
                context.setHint(Hints.CREATE_TEMPLATE);
                return "Credential selected, id: " + id;
            } else {
                return "No credential specified";
            }
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential createEC2", help = "Create a new EC2 credential")
    public String createEc2Credential(
            @CliOption(key = "description", mandatory = true, help = "Description of the credential") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "roleArn", mandatory = true, help = "roleArn of the credential") String roleArn,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "sshKeyPath of the template") String sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "sshKeyUrl of the template") String sshKeyUrl
    ) {
        if ((sshKeyPath == null || sshKeyPath.isEmpty()) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "SshKey cannot be null if password null";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath))).replaceAll("\n", "");
            } catch (IOException e) {
                return "File not found with ssh key.";
            }
        } else {
            try {
                sshKey = readUrl(sshKeyUrl);
            } catch (IOException e) {
                return "Url not found with ssh key.";
            }
        }
        try {
            String id = cloudbreak.postEc2Credential(name, description, roleArn, sshKey);
            context.setCredential(id);
            context.setHint(Hints.CREATE_TEMPLATE);
            return "Credential created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliAvailabilityIndicator(value = "credential createAZURE")
    public boolean isCredentialAzureCreateCommandAvailable() {
        return true;
    }

    private String readUrl(String url) throws IOException {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        String str;
        StringBuffer sb = new StringBuffer();
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();
        return sb.toString();
    }

    @CliCommand(value = "credential createAZURE", help = "Create a new AZURE credential")
    public String createAzureCredential(
            @CliOption(key = "description", mandatory = true, help = "Description of the credential") String description,
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "subscriptionId", mandatory = true, help = "subscriptionId of the credential") String subscriptionId,
            @CliOption(key = "jksPassword", mandatory = true, help = "jksPassword of the credential") String jksPassword,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "sshKeyPath of the template") String sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "sshKeyUrl of the template") String sshKeyUrl
    ) {
        if ((sshKeyPath == null || sshKeyPath.isEmpty()) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "SshKey cannot be null if password null";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath))).replaceAll("\n", "");
            } catch (IOException e) {
                return "File not found with ssh key.";
            }
        } else {
            try {
                sshKey = readUrl(sshKeyUrl);
            } catch (IOException e) {
                return "Url not found with ssh key.";
            }
        }
        try {
            String id = cloudbreak.postAzureCredential(name, description, subscriptionId, jksPassword, sshKey);
            context.setCredential(id);
            context.setHint(Hints.CREATE_TEMPLATE);
            return "Credential created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliAvailabilityIndicator(value = "credential certificate")
    public boolean isCredentialCertificateCommandAvailable() {
        try {
            List<Map> credentials = cloudbreak.getCredentials();
            int count = 0;
            maps = new ArrayList<>();
            for (Map map : credentials) {
                if (map.get("cloudPlatform").toString().equals(AZURE)) {
                    maps.add(map);
                    count++;
                }
            }
            return count == 0 ? false : true;
        } catch (Exception ex) {
            return false;
        }
    }

    @CliCommand(value = "credential certificate", help = "get Azure certificate")
    public String getAzureCertificate(
            @CliOption(key = "id", mandatory = true, help = "id of the credential") String id,
            @CliOption(key = "resultPath", mandatory = true, help = "path of the certificate") String path
    ) {
        try {
            for (Map map : maps) {
                if (map.get("id").toString().equals(id)) {
                    String certicate = cloudbreak.getCertificate(id);
                    FileUtils.writeStringToFile(new File(path, "certicate.cer"), certicate);
                    return "Your certicate was saved into " + String.format("%s/%s", path, "certicate.cer")
                            + "\nPlease upload to the Azure portal before you continue"
                            + "\nThe file contains your certificate which is:\n" + certicate;
                }
            }
            return "Azure certificate with this id does not exist";
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }
}
