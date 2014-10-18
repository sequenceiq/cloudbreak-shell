package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.AzureInstanceName;
import com.sequenceiq.cloudbreak.shell.model.AzureLocation;
import com.sequenceiq.cloudbreak.shell.model.AzureVmType;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.GccImageName;
import com.sequenceiq.cloudbreak.shell.model.GccInstanceType;
import com.sequenceiq.cloudbreak.shell.model.GccZone;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.InstanceType;
import com.sequenceiq.cloudbreak.shell.model.Region;
import com.sequenceiq.cloudbreak.shell.model.VolumeType;

import groovyx.net.http.HttpResponseException;

@Component
public class TemplateCommands implements CommandMarker {

    public static final int BUFFER = 1024;
    public static final int VOLUME_COUNT_MIN = 1;
    public static final int VOLUME_COUNT_MAX = 8;
    public static final int VOLUME_SIZE_MIN = 1;
    public static final int VOLUME_SIZE_MAX = 1024;
    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @CliAvailabilityIndicator(value = "template list")
    public boolean isTemplateListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template show")
    public boolean isTemplateShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template delete")
    public boolean isTemplateDeleteCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template createEC2")
    public boolean isTemplateEc2CreateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template createGcc")
    public boolean isTemplateGccCreateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template createAZURE")
    public boolean isTemplateAzureCreateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "template select")
    public boolean isTemplateSelectCommandAvailable() {
        return true;
    }

    @CliCommand(value = "template list", help = "Shows the currently available cloud templates")
    public String listTemplates() {
        try {
            Map<String, String> templatesMap = cloudbreak.getAccountTemplatesMap();
            return renderSingleMap(templatesMap, "ID", "INFO");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @CliCommand(value = "template select", help = "Select the template by its id")
    public String selectTemplate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        try {
            Object template = cloudbreak.getTemplate(id);
            if (template != null) {
                context.addTemplate(id);
                context.setHint(Hints.ADD_BLUEPRINT);
                return "Template selected, id: " + id;
            } else {
                return "No template specified";
            }
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "template createEC2", help = "Create a new EC2 template")
    public String createEc2Template(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "description", mandatory = true, help = "Description of the template") String description,
            @CliOption(key = "region", mandatory = true, help = "region of the template") Region region,
            @CliOption(key = "instanceType", mandatory = true, help = "instanceType of the template") InstanceType instanceType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "volumeType", mandatory = false, help = "volumeType of the template", specifiedDefaultValue = "Gp2") VolumeType volumeType,
            @CliOption(key = "spotPrice", mandatory = false, help = "spotPrice of the template") Double spotPrice,
            @CliOption(key = "sshLocation", mandatory = false, specifiedDefaultValue = "0.0.0.0/0", help = "sshLocation of the template") String sshLocation,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount
    ) {
        try {
            if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
                return "volumeCount has to be between 1 and 8.";
            }
            if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
                return "VolumeSize has to be between 1 and 1024.";
            }
            if (volumeType == null) {
                volumeType = VolumeType.Gp2;
            }
            String id;
            if (spotPrice == null) {
                id = cloudbreak.postEc2Template(name,
                        description,
                        region.name(),
                        region.getAmi(),
                        sshLocation == null ? "0.0.0.0/0" : sshLocation,
                        instanceType.toString(),
                        volumeCount.toString(),
                        volumeSize.toString(),
                        volumeType.name(),
                        publicInAccount
                );
            } else {
                id = cloudbreak.postSpotEc2Template(name,
                        description,
                        region.name(),
                        region.getAmi(),
                        sshLocation == null ? "0.0.0.0/0" : sshLocation,
                        instanceType.toString(),
                        volumeCount.toString(),
                        volumeSize.toString(),
                        volumeType.name(),
                        spotPrice.toString(),
                        publicInAccount
                );
            }
            context.addTemplate(id);
            context.setHint(Hints.ADD_BLUEPRINT);
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }


    @CliCommand(value = "template createAZURE", help = "Create a new AZURE template")
    public String createAzureTemplate(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "description", mandatory = true, help = "Description of the template") String description,
            @CliOption(key = "location", mandatory = true, help = "location of the template") AzureLocation location,
            @CliOption(key = "instanceType", mandatory = true, help = "type of the VM") AzureVmType vmType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "imageName", mandatory = false, help = "instance name of the template: AMBARI_DOCKER_V1") AzureInstanceName imageName,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount
    ) {
        if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
            return "volumeCount has to be between 1 and 8.";
        }
        if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
            return "VolumeSize has to be between 1 and 1024.";
        }
        if (imageName == null) {
            imageName = AzureInstanceName.AMBARI_DOCKER_V1;
        }
        try {
            String id = cloudbreak.postAzureTemplate(name,
                    description,
                    location.name(),
                    imageName.name(),
                    vmType.name(),
                    volumeCount.toString(),
                    volumeSize.toString(),
                    publicInAccount
            );
            context.addTemplate(id);
            context.setHint(Hints.ADD_BLUEPRINT);
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "template createGcc", help = "Create a new GCC template")
    public String createGccTemplate(
            @CliOption(key = "name", mandatory = true, help = "Name of the template") String name,
            @CliOption(key = "description", mandatory = true, help = "Description of the template") String description,
            @CliOption(key = "location", mandatory = true, help = "location of the template") GccZone location,
            @CliOption(key = "instanceType", mandatory = true, help = "type of the VM") GccInstanceType instanceType,
            @CliOption(key = "volumeCount", mandatory = true, help = "volumeCount of the template") Integer volumeCount,
            @CliOption(key = "volumeSize", mandatory = true, help = "volumeSize(GB) of the template") Integer volumeSize,
            @CliOption(key = "imageName", mandatory = false, help = "instance name of the template: DEBIAN_HACK") GccImageName imageName,
            @CliOption(key = "publicInAccount", mandatory = false, help = "flags if the template is public in the account") Boolean publicInAccount
    ) {
        if (volumeCount < VOLUME_COUNT_MIN || volumeCount > VOLUME_COUNT_MAX) {
            return "volumeCount has to be between 1 and 8.";
        }
        if (volumeSize < VOLUME_SIZE_MIN || volumeSize > VOLUME_SIZE_MAX) {
            return "VolumeSize has to be between 1 and 1024.";
        }
        if (imageName == null) {
            imageName = GccImageName.DEBIAN_HACK;
        }
        try {
            String id = cloudbreak.postGccTemplate(name,
                    description,
                    imageName.name(),
                    instanceType.name(),
                    volumeCount.toString(),
                    volumeSize.toString(),
                    location.name(),
                    publicInAccount
            );
            context.addTemplate(id);
            context.setHint(Hints.ADD_BLUEPRINT);
            return "Template created, id: " + id;
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }


    @CliCommand(value = "template show", help = "Shows the template by its id")
    public Object showTemlate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        try {
            return renderSingleMap(cloudbreak.getTemplateMap(id), "FIELD", "VALUE");
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @CliCommand(value = "template delete", help = "Shows the template by its id")
    public Object deleteTemlate(
            @CliOption(key = "id", mandatory = true, help = "Id of the template") String id) {
        try {
            return cloudbreak.deleteTemplate(id);
        } catch (HttpResponseException ex) {
            return ex.getResponse().getData().toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[BUFFER];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
