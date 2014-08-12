package com.sequenceiq.cloudbreak.shell.model;

public enum VolumeType {

    Standard("standard"),
    Io1("io1"),
    Gp2("gp2");

    private String value;

    private VolumeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
