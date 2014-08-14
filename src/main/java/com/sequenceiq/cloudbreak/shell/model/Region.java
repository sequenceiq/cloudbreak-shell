package com.sequenceiq.cloudbreak.shell.model;

public enum Region {

    US_EAST_1("ami-0609d86e"),
    US_WEST_1("ami-73000d36"),
    US_WEST_2("ami-07bdc737"),
    EU_WEST_1("ami-e09e4397"),
    AP_SOUTHEAST_1("ami-363f6464"),
    AP_SOUTHEAST_2("ami-3d9ef807"),
    AP_NORTHEAST_1("ami-8dbde48c"),
    SA_EAST_1("ami-17f45c0a");

    private String ami;

    Region(String ami) {
        this.ami = ami;
    }

    public String getAmi() {
        return ami;
    }
}
