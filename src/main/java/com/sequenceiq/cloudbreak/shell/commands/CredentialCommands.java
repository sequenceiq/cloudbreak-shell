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

import org.apache.commons.codec.binary.Base64;
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
    public boolean isCredentialSelectCommandAvailable() throws Exception {
        return context.isCredentialAccessible();
    }

    @CliAvailabilityIndicator({ "credential create --GCP", "credential create --EC2", "credential create --AZURE", "credential create --OPENSTACK" })
    public boolean isCredentialEc2CreateCommandAvailable() {
        return true;
    }

    @CliCommand(value = "credential show", help = "Shows the credential by its id")
    public Object showCredential(
            @CliOption(key = "id", mandatory = false, help = "Id of the credential") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the credential") String name) {
        try {
            if (id != null) {
                return renderSingleMap(cloudbreak.getCredentialMap(id), "FIELD", "VALUE");
            } else if (name != null) {
                Object credential = cloudbreak.getCredentialByName(name);
                if (credential != null) {
                    Map<String, Object> credMap = (Map<String, Object>) credential;
                    String credId = credMap.get("id").toString();
                    return renderSingleMap(cloudbreak.getCredentialMap(credId), "FIELD", "VALUE");
                }
            }
            return "No credential specified (select a credential by --id or --name)";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential delete", help = "Delete the credential by its id")
    public Object deleteCredential(
            @CliOption(key = "id", mandatory = false, help = "Id of the credental") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the credential") String name) {
        try {
            if (id != null) {
                return cloudbreak.deleteCredential(id);
            } else if (name != null) {
                return cloudbreak.deleteCredentialByName(name);
            }
            return "No credential specified (select a credential by --id or --name)";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential list", help = "Shows all of your credentials")
    public String listCredentials() {
        try {
            return renderSingleMap(cloudbreak.getAccountCredentialsMap(), "ID", "INFO");
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential select", help = "Select the credential by its id or name")
    public String selectCredential(
            @CliOption(key = "id", mandatory = false, help = "Id of the credential") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the credential") String name) {
        try {

            if (id != null) {
                if (cloudbreak.getCredential(id) != null) {
                    context.setCredential(id);
                    createOrSelectTemplateHint();
                    return "Credential selected, id: " + id;
                }
            } else if (name != null) {
                Object credential = cloudbreak.getCredentialByName(name);
                if (credential != null) {
                    Map<String, Object> credMap = (Map<String, Object>) credential;
                    context.setCredential(credMap.get("id").toString());
                    createOrSelectTemplateHint();
                    return "Credential selected, name: " + name;
                }
            }
            return "No credential specified (select a credential by --id or --name)";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential create --OPENSTACK", help = "Create a new OPENSTACK credential")
    public String createOpenStackCredential(
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "userName", mandatory = true, help = "Username of the credential") String userName,
            @CliOption(key = "password", mandatory = true, help = "password of the credential") String password,
            @CliOption(key = "tenantName", mandatory = true, help = "tenantName of the credential") String tenantName,
            @CliOption(key = "endPoint", mandatory = true, help = "endPoint of the credential") String endPoint,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "path of a public SSH key file") File sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "URL of a public SSH key file") String sshKeyUrl,
            @CliOption(key = "description", mandatory = false, help = "Description of the credential") String description,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the credential is public in the account") Boolean publicInAccount
        ) {
        if ((sshKeyPath == null) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "An SSH public key must be specified either with --sshKeyPath or --sshKeyUrl";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath.getPath()))).replaceAll("\n", "");
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
            String id = cloudbreak.postOpenStackCredential(name, description, userName, password, tenantName, endPoint, sshKey, publicInAccount);
            context.setCredential(id);
            createOrSelectTemplateHint();
            return "Credential created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential create --EC2", help = "Create a new EC2 credential")
    public String createEc2Credential(
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "roleArn", mandatory = true, help = "roleArn of the credential") String roleArn,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "path of a public SSH key file") File sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "URL of a public SSH key file") String sshKeyUrl,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the credential is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the template") String description
            ) {
        if ((sshKeyPath == null) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "An SSH public key must be specified either with --sshKeyPath or --sshKeyUrl";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath.getPath()))).replaceAll("\n", "");
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
            String id = cloudbreak.postEc2Credential(
                    name,
                    description == null ? "Aws credential was created by the cloudbreak-shell" : description,
                    roleArn,
                    sshKey,
                    publicInAccount == null ? false : publicInAccount
            );
            context.setCredential(id);
            createOrSelectTemplateHint();
            return "Credential created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "credential create --GCP", help = "Create a new Gcp credential")
    public String createGcpCredential(
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "projectId", mandatory = true, help = "projectId of the credential") String projectId,
            @CliOption(key = "serviceAccountId", mandatory = true, help = "serviceAccountId of the credential") String serviceAccountId,
            @CliOption(key = "serviceAccountPrivateKeyPath", mandatory = true, help = "path of a service account private key (p12) file")
            File serviceAccountPrivateKeyPath,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "path of a public SSH key file") File sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "URL of a public SSH key url") String sshKeyUrl,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the credential is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the credential") String description
            ) {
        if ((sshKeyPath == null) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "An SSH public key must be specified either with --sshKeyPath or --sshKeyUrl";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = new String(Files.readAllBytes(Paths.get(sshKeyPath.getPath()))).replaceAll("\n", "");
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

        String serviceAccountPrivateKey;

        try {
            serviceAccountPrivateKey = Base64.encodeBase64String(Files.readAllBytes(serviceAccountPrivateKeyPath.toPath())).replaceAll("\n", "");
        } catch (IOException e) {
            return "File not found with service account private key (p12) file.";
        }

        try {
            String id = cloudbreak.postGcpCredential(
                    name,
                    description == null ? "Gcp credential was created by the cloudbreak-shell" : description,
                    sshKey,
                    publicInAccount == null ? false : publicInAccount,
                    projectId,
                    serviceAccountId,
                    serviceAccountPrivateKey
            );
            context.setCredential(id);
            createOrSelectTemplateHint();
            return "Credential created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
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

    @CliCommand(value = "credential create --AZURE", help = "Create a new AZURE credential")
    public String createAzureRmCredential(
            @CliOption(key = "name", mandatory = true, help = "Name of the credential") String name,
            @CliOption(key = "subscriptionId", mandatory = true, help = "subscriptionId of the credential") String subscriptionId,
            @CliOption(key = "tenantId", mandatory = true, help = "tenantId of the credential") String tenantId,
            @CliOption(key = "appId", mandatory = true, help = "appId of the credential") String appId,
            @CliOption(key = "password", mandatory = true, help = "password of the credential") String password,
            @CliOption(key = "sshKeyPath", mandatory = false, help = "sshKeyPath of the template") File sshKeyPath,
            @CliOption(key = "sshKeyUrl", mandatory = false, help = "sshKeyUrl of the template") String sshKeyUrl,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the credential is public in the account") Boolean publicInAccount,
            @CliOption(key = "description", mandatory = false, help = "Description of the credential") String description
    ) {
        if ((sshKeyPath == null) && (sshKeyUrl == null || sshKeyUrl.isEmpty())) {
            return "SshKey cannot be null if password null";
        }
        String sshKey;
        if (sshKeyPath != null) {
            try {
                sshKey = Base64.encodeBase64String(Files.readAllBytes(Paths.get(sshKeyPath.getPath())));
            } catch (IOException e) {
                return "File not found with ssh key.";
            }
        } else {
            try {
                sshKey = Base64.encodeBase64String(readUrl(sshKeyUrl).getBytes());
            } catch (IOException e) {
                return "Url not found with ssh key.";
            }
        }
        try {
            String id = cloudbreak.postAzureRmCredential(
                    name,
                    description == null ? "Azure Rm credential was created by the cloudbreak-shell" : description,
                    subscriptionId,
                    tenantId,
                    appId,
                    password,
                    sshKey,
                    publicInAccount == null ? false : publicInAccount
            );
            context.setCredential(id);
            createOrSelectTemplateHint();
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
            List<Map> credentials = cloudbreak.getAccountCredentials();
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

    private void createOrSelectTemplateHint() throws Exception {
        if (cloudbreak.getAccountBlueprints().isEmpty()) {
            context.setHint(Hints.ADD_BLUEPRINT);
        } else {
            context.setHint(Hints.SELECT_BLUEPRINT);
        }
    }
}
