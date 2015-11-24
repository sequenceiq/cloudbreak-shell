package com.sequenceiq.cloudbreak.shell;

import static com.sequenceiq.cloudbreak.shell.VersionedApplication.versionedApplication;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.CommandLine;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.event.ShellStatus;
import org.springframework.shell.event.ShellStatusListener;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;

@Configuration
@ComponentScan(basePackageClasses = { CloudbreakShell.class })
public class CloudbreakShell implements CommandLineRunner, ShellStatusListener {

    public static final String DOLLAR = "$";
    public static final String SPACE = " ";
    public static final String EMPTY = "";
    public static final String FAILED = "FAILED";
    public static final String SUCCESS = "SUCCESS";

    @Autowired
    private CommandLine commandLine;
    @Autowired
    private JLineShellComponent shell;
    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @Value("${sequenceiq.user:}")
    private String user;

    @Value("${sequenceiq.password:}")
    private String password;

    @Override
    public void run(String... arg) throws Exception {
        if ("".equals(user)) {
            System.out.println("Missing 'sequenceiq.user' parameter!");
            return;
        }
        if ("".equals(password)) {
            System.out.println("Missing 'sequenceiq.password' parameter!");
            return;
        }
        String[] shellCommandsToExecute = commandLine.getShellCommandsToExecute();
        if (shellCommandsToExecute != null) {
            init();
            for (String cmd : shellCommandsToExecute) {
                String replacedCommand = getReplacedString(cmd);
                if (!shell.executeScriptLine(replacedCommand)) {
                    System.out.println(String.format("%s: [%s]", replacedCommand, FAILED));
                    break;
                } else {
                    System.out.println(String.format("%s: [%s]", replacedCommand, SUCCESS));
                }
            }
        } else {
            shell.addShellStatusListener(this);
            shell.start();
            shell.promptLoop();
            shell.waitForComplete();
        }
    }

    private String getReplacedString(String cmd) {
        String result = cmd;
        if (result != null) {
            for (String split : cmd.split(SPACE)) {
                if (split.startsWith(DOLLAR)) {
                    result = result.replace(split, System.getenv(split.replace(DOLLAR, EMPTY)));
                }
            }
        }
        return result;
    }

    @Override
    public void onShellStatusChange(ShellStatus oldStatus, ShellStatus newStatus) {
        if (newStatus.getStatus() == ShellStatus.Status.STARTED) {
            try {
                init();
            } catch (Exception e) {
                System.out.println("Can't connect to Cloudbreak");
                e.printStackTrace();
                shell.executeCommand("quit");
            }
        }
    }

    private void init() throws Exception {
        cloudbreak.health();
        initResourceAccessibility();
        initPlatformVariants();
        if (!context.isCredentialAccessible()) {
            context.setHint(Hints.CREATE_CREDENTIAL);
        } else {
            context.setHint(Hints.SELECT_CREDENTIAL);
        }
    }

    public static void main(String[] args) throws IOException {
        if ((args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) || args.length == 0) {
            System.out.println(
                    "\nCloudbreak Shell: Interactive command line tool for managing Cloudbreak.\n\n"
                            + "Usage:\n"
                            + "  java -jar cloudbreak-shell.jar                  : Starts Cloudbreak Shell in interactive mode.\n"
                            + "  java -jar cloudbreak-shell.jar --cmdfile=<FILE> : Cloudbreak Shell executes commands read from the file.\n\n"
                            + "Options:\n"
                            + "  --cloudbreak.address=<http[s]://HOSTNAME:PORT>  Address of the Cloudbreak Server"
                            + " [default: https://cloudbreak-api.sequenceiq.com].\n"
                            + "  --identity.address=<http[s]://HOSTNAME:PORT>    Address of the SequenceIQ identity server"
                            + " [default: https://identity.sequenceiq.com].\n"
                            + "  --sequenceiq.user=<USER>                        Username of the SequenceIQ user [default: user@sequenceiq.com].\n"
                            + "  --sequenceiq.password=<PASSWORD>                Password of the SequenceIQ user [default: password].\n"
                            + "Note:\n"
                            + "  Please specify the username and password of your SequenceIQ user."
            );
            return;
        } else {
            if (!versionedApplication().showVersionInfo(args)) {
                try {
                    new SpringApplicationBuilder(CloudbreakShell.class).showBanner(false).run(args);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Cloudbreak shell cannot be started.");
                }
            }
        }
    }

    private void initResourceAccessibility() throws Exception {
        if (!cloudbreak.getAccountCredentials().isEmpty()) {
            context.setCredentialAccessible();
        }
        if (!cloudbreak.getAccountBlueprints().isEmpty()) {
            context.setBlueprintAccessible();
        }
        if (!cloudbreak.getAccountStacks().isEmpty()) {
            context.setStackAccessible();
        }
        if (!cloudbreak.getAccountRecipes().isEmpty()) {
            context.setRecipeAccessible();
        }
        List<Map> accountNetworks = cloudbreak.getAccountNetworks();
        for (Map network : accountNetworks) {
            context.putNetwork(network.get("id").toString(), network.get("cloudPlatform").toString());
        }
        List<Map> securityGroups = cloudbreak.getAccountSecurityGroups();
        for (Map securityGroup : securityGroups) {
            context.putSecurityGroup(securityGroup.get("id").toString(), securityGroup.get("name").toString());
        }
    }

    private void initPlatformVariants() {
        Map<String, Collection<String>> platformToVariants = Collections.EMPTY_MAP;
        Map<String, Collection<String>> regions = Collections.EMPTY_MAP;
        Map<String, Collection<String>> volumeTypes = Collections.EMPTY_MAP;
        Map<String, Map<String, Collection<String>>> availabilityZones = Collections.EMPTY_MAP;
        Map<String, Collection<Map<String, String>>> instanceTypes = Collections.EMPTY_MAP;
        try {
            Map<String, Map<String, Map>> cloudConnectorParams = (Map<String, Map<String, Map>>) cloudbreak.getCloudConnectorParams();
            platformToVariants = (Map<String, Collection<String>>) cloudConnectorParams.get("variants").get("platformToVariants");
            regions = (Map<String, Collection<String>>) cloudConnectorParams.get("regions").get("regions");
            regions.put("AZURE", regions.get("AZURE_RM"));
            availabilityZones = (Map<String, Map<String, Collection<String>>>) cloudConnectorParams.get("regions").get("availabilityZones");
            volumeTypes = (Map<String, Collection<String>>) cloudConnectorParams.get("disks").get("diskTypes");
            instanceTypes = cloudConnectorParams.get("virtualMachines").get("virtualMachines");
        } catch (Exception e) {
            System.out.println("Error during retrieving platform variants");
        } finally {
            context.setPlatformToVariantsMap(platformToVariants);
            context.setRegions(regions);
            context.setAvailabilityZones(availabilityZones);
            context.setVolumeTypes(volumeTypes);
            context.setInstanceTypes(instanceTypes);
        }
    }

}
