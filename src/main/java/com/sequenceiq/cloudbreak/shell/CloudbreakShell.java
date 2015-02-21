package com.sequenceiq.cloudbreak.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
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

    @Autowired
    private CommandLine commandLine;
    @Autowired
    private JLineShellComponent shell;
    @Autowired
    private CloudbreakContext context;
    @Autowired
    private CloudbreakClient cloudbreak;

    @Override
    public void run(String... arg) throws Exception {
        String[] shellCommandsToExecute = commandLine.getShellCommandsToExecute();
        if (shellCommandsToExecute != null) {
            for (String cmd : shellCommandsToExecute) {
                if (!shell.executeScriptLine(cmd)) {
                    break;
                }
            }
        } else {
            shell.addShellStatusListener(this);
            shell.start();
            shell.promptLoop();
            shell.waitForComplete();
        }
    }

    @Override
    public void onShellStatusChange(ShellStatus oldStatus, ShellStatus newStatus) {
        if (newStatus.getStatus() == ShellStatus.Status.STARTED) {
            try {
                cloudbreak.health();
                initResourceAccessibility();
                if (!context.isCredentialAccessible()) {
                    context.setHint(Hints.CREATE_CREDENTIAL);
                } else {
                    context.setHint(Hints.SELECT_CREDENTIAL);
                }
            } catch (Exception e) {
                System.out.println("Can't connect to Cloudbreak");
                e.printStackTrace();
                shell.executeCommand("quit");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
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
        }
        if (args.length == 1 && ("--version".equals(args[0]) || "-v".equals(args[0]))) {
            System.out.println("Cloudbreak shell version is: \n" + readFileFromClasspath("application.properties"));
            return;
        }
        try {
            new SpringApplicationBuilder(CloudbreakShell.class).showBanner(false).run(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Cloudbreak shell cannot be started.");
        }
    }

    public static final String readFileFromClasspath(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(new ClassPathResource(fileName).getInputStream(), "UTF-8"));
        for (int c = br.read(); c != -1; c = br.read()) {
            sb.append((char) c);
        }
        return sb.toString();
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
    }
}
