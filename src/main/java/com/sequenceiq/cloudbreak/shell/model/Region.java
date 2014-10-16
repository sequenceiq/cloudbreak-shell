package com.sequenceiq.cloudbreak.shell.model;

public enum Region {

    US_EAST_1("ami-5edf6136"),
    US_WEST_1("ami-4fc1cb0a"),
    US_WEST_2("ami-29367a19"),
    EU_WEST_1("ami-e66cc191"),
    AP_SOUTHEAST_1("ami-7cedcb2e"),
    AP_SOUTHEAST_2("ami-2f385515"),
    AP_NORTHEAST_1("ami-757e4974"),
    SA_EAST_1("ami-91f0448c");

    private String ami;

    Region(String ami) {
        this.ami = ami;
    }

    public String getAmi() {
        return ami;
    }
}
