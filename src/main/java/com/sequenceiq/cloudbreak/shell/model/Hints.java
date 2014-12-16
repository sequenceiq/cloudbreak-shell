package com.sequenceiq.cloudbreak.shell.model;

/**
 * Provides some guidance's to the user, what he/she can follow.
 */
public enum Hints {

    CREATE_CREDENTIAL("Create a new credential with the 'credential create' command"),
    SELECT_CREDENTIAL("Create a new credential with the 'credential create' command or select an existing one with 'credential select'"),
    ADD_BLUEPRINT("Add a blueprint with the 'blueprint add' command"),
    SELECT_BLUEPRINT("Add a blueprint with the 'blueprint add' command or select an existing one with 'blueprint select'"),
    CREATE_TEMPLATE("Create a cloud template with the 'template create' command"),
    SELECT_TEMPLATE("Create a cloud template with the 'template create' command or select an existing one with 'blueprint select'"),
    CREATE_STACK("Create a stack based on a template with the 'stack create' command"),
    SELECT_STACK("Create a stack based on a template with the 'stack create' command or select an existing one with 'stack select'"),
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
