package com.sequenceiq.cloudbreak.shell.model;

public enum Region {

    US_EAST_1("ami-d89955b0"),
    US_WEST_1("ami-314a4974"),
    US_WEST_2("ami-17b0c927"),
    EU_WEST_1("ami-7778af00"),
    AP_SOUTHEAST_1("ami-468bd214"),
    AP_SOUTHEAST_2("ami-678bec5d"),
    AP_NORTHEAST_1("ami-0bcd9d0a"),
    SA_EAST_1("ami-1b0ca206");

    private String ami;

    Region(String ami) {
        this.ami = ami;
    }

    public String getAmi() {
        return ami;
    }
}
