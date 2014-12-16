package com.sequenceiq.cloudbreak.shell;

import org.springframework.beans.factory.annotation.Autowired;
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
                if (cloudbreak.getAccountCredentials().isEmpty()) {
                    context.setHint(Hints.CREATE_CREDENTIAL);
                } else {
                    context.setHint(Hints.SELECT_CREDENTIAL);
                }
            } catch (Exception e) {
                System.out.println("Can't connect to Cloudbreak");
                shell.executeCommand("quit");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
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
        try {
            new SpringApplicationBuilder(CloudbreakShell.class).showBanner(false).run(args);
        } catch (Exception e) {
            System.out.println("Cloudbreak shell cannot be started.");
        }
    }
}
