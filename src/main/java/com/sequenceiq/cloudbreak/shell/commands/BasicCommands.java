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
package com.sequenceiq.cloudbreak.shell.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;

/**
 * Basic commands used in the shell. Delegating the commands
 * to the Cloudbreak server via a Groovy based client.
 */
@Component
public class BasicCommands implements CommandMarker {

    private CloudbreakContext context;

    @Autowired
    public BasicCommands(CloudbreakContext context) {
        this.context = context;
    }

    /**
     * Checks whether the hint command is available or not.
     *
     * @return true if available false otherwise
     */
    @CliAvailabilityIndicator("hint")
    public boolean isHintCommandAvailable() {
        return true;
    }

    /**
     * Provides some hints what you can do in the current context.
     *
     * @return hint message
     */
    @CliCommand(value = "hint", help = "Shows some hints")
    public String hint() {
        return context.getHint();
    }
}
