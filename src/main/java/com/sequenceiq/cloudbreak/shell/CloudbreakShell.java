/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sequenceiq.cloudbreak.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.CommandLine;
import org.springframework.shell.core.JLineShellComponent;

@Configuration
@ComponentScan(basePackageClasses = {CloudbreakShell.class})
public class CloudbreakShell implements CommandLineRunner {

    @Autowired
    private CommandLine commandLine;
    @Autowired
    private JLineShellComponent shell;

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
            shell.start();
            shell.promptLoop();
            shell.waitForComplete();
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                    "\nCloudbreak Shell: Interactive command line tool for managing Cloudbreak.\n\n" +
                            "Usage:\n" +
                            "  java -jar cloudbreak-shell.jar                  : Starts Cloudbreak Shell in interactive mode.\n" +
                            "  java -jar cloudbreak-shell.jar --cmdfile=<FILE> : Cloudbreak Shell executes commands read from the file.\n\n" +
                            "Options:\n" +
                            "  --cloudbreak.host=<HOSTNAME>       Hostname of the Ambari Server [default: localhost].\n" +
                            "  --cloudbreak.port=<PORT>           Port of the Ambari Server [default: 8080].\n" +
                            "  --cloudbreak.user=<USER>           Username of the Ambari admin [default: user@seq.com].\n" +
                            "  --cloudbreak.password=<PASSWORD>   Password of the Ambari admin [default: test123].\n\n" +
                            "Note:\n" +
                            "  At least one option is mandatory."
            );
            System.exit(1);
        }
        new SpringApplicationBuilder(CloudbreakShell.class).showBanner(false).run(args);
    }
}
