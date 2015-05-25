package com.sequenceiq.cloudbreak.shell.model;

import java.util.HashSet;
import java.util.Set;

public enum Region {
    US_CENTRAL1_A("GCP"),
    US_CENTRAL1_B("GCP"),
    US_CENTRAL1_F("GCP"),
    EUROPE_WEST1_B("GCP"),
    ASIA_EAST1_A("GCP"),
    ASIA_EAST1_B("GCP"),
    US_EAST_1("AWS"),
    US_WEST_1("AWS"),
    US_WEST_2("AWS"),
    EU_WEST_1("AWS"),
    AP_SOUTHEAST_1("AWS"),
    AP_SOUTHEAST_2("AWS"),
    AP_NORTHEAST_1("AWS"),
    SA_EAST_1("AWS"),
    EAST_ASIA("AZURE"),
    NORTH_EUROPE("AZURE"),
    WEST_EUROPE("AZURE"),
    EAST_US("AZURE"),
    CENTRAL_US("AZURE"),
    SOUTH_CENTRAL_US("AZURE"),
    NORTH_CENTRAL_US("AZURE"),
    EAST_US_2("AZURE"),
    WEST_US("AZURE"),
    JAPAN_EAST("AZURE"),
    JAPAN_WEST("AZURE"),
    SOUTHEAST_ASIA("AZURE"),
    BRAZIL_SOUTH("AZURE"),
    local("OPENSTACK");

    private String cloudPlatform;

    Region(String cloudPlatform) {
        this.cloudPlatform = cloudPlatform;
    }

    public static Set<String> filterRegions(String cloudPlatform) {
        Set<String> regions = new HashSet<>();
        for (Region region : Region.values()) {
            if (region.cloudPlatform.equals(cloudPlatform)) {
                regions.add(region.name());
            }
        }
        return regions;
    }
}
