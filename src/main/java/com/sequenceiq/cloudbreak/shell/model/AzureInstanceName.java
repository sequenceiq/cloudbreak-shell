package com.sequenceiq.cloudbreak.shell.model;

public enum AzureInstanceName {
    AMBARI_DOCKER_V1("ambari-docker-v1");

    private String value;

    AzureInstanceName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
