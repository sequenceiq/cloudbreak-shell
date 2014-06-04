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
package com.sequenceiq.cloudbreak.shell.model;

/**
 * Provides some guidance's to the user, what he/she can follow.
 */
public enum Hints {

    CREATE_CREDENTIAL("Create a new credential with the 'credential create' command"),
    ADD_BLUEPRINT("Add a blueprint with the 'blueprint add' command"),
    CREATE_TEMPLATE("Create a cloud template with the 'template create' command"),
    CREATE_STACK("Create a stack based on a template with the 'stack create' command"),
    CREATE_CLUSTER("Create a cluster with the 'cluster create' command"),
    NONE("No more hints for you.. :(");

    private final String message;

    private Hints(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
