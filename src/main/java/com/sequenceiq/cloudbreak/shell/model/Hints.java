package com.sequenceiq.cloudbreak.shell.model;

/**
 * Provides some guidance's to the user, what he/she can follow.
 */
public enum Hints {

    CREATE_CREDENTIAL("Create a new credential with the 'credential create' command"),
    ADD_BLUEPRINT("Add a blueprint with the 'blueprint add' command"),
    CREATE_TEMPLATE("Create a cloud template with the 'template create' command"),
    SELECT_TEMPLATE("Select a cloud template with the 'select create' command"),
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
