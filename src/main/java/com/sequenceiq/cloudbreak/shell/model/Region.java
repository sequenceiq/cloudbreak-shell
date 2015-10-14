package com.sequenceiq.cloudbreak.shell.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Region {
    US_CENTRAL1_A("GCP", Arrays.<String>asList()),
    US_CENTRAL1_B("GCP", Arrays.<String>asList()),
    US_CENTRAL1_F("GCP", Arrays.<String>asList()),
    EUROPE_WEST1_B("GCP", Arrays.<String>asList()),
    ASIA_EAST1_A("GCP", Arrays.<String>asList()),
    ASIA_EAST1_B("GCP", Arrays.<String>asList()),
    US_EAST_1("AWS", Arrays.<String>asList("us-east-1a", "us-east-1b", "us-east-1d", "us-east-1e")),
    US_WEST_1("AWS", Arrays.<String>asList("us-west-1a", "us-west-1b")),
    US_WEST_2("AWS", Arrays.<String>asList("us-west-2a", "us-west-2b", "us-west-2c")),
    EU_WEST_1("AWS", Arrays.<String>asList("eu-west-1a", "eu-west-1b", "eu-west-1c")),
    EU_CENTRAL_1("AWS", Arrays.<String>asList("eu-central-1a", "eu-central-1b")),
    AP_SOUTHEAST_1("AWS", Arrays.<String>asList("ap-southeast-1a", "ap-southeast-1b")),
    AP_SOUTHEAST_2("AWS", Arrays.<String>asList("ap-southeast-2a", "ap-southeast-2b")),
    AP_NORTHEAST_1("AWS", Arrays.<String>asList("ap-northeast-1a", "ap-northeast-1c")),
    SA_EAST_1("AWS", Arrays.<String>asList("sa-east-1a", "sa-east-1b", "sa-east-1c")),
    EAST_ASIA("AZURE", Arrays.<String>asList()),
    NORTH_EUROPE("AZURE", Arrays.<String>asList()),
    WEST_EUROPE("AZURE", Arrays.<String>asList()),
    EAST_US("AZURE", Arrays.<String>asList()),
    CENTRAL_US("AZURE", Arrays.<String>asList()),
    SOUTH_CENTRAL_US("AZURE", Arrays.<String>asList()),
    NORTH_CENTRAL_US("AZURE", Arrays.<String>asList()),
    EAST_US_2("AZURE", Arrays.<String>asList()),
    WEST_US("AZURE", Arrays.<String>asList()),
    JAPAN_EAST("AZURE", Arrays.<String>asList()),
    JAPAN_WEST("AZURE", Arrays.<String>asList()),
    SOUTHEAST_ASIA("AZURE", Arrays.<String>asList()),
    BRAZIL_SOUTH("AZURE", Arrays.<String>asList()),
    local("OPENSTACK", Arrays.<String>asList());

    private String cloudPlatform;
    private List<String> avZones;

    Region(String cloudPlatform, List<String> avZones) {
        this.cloudPlatform = cloudPlatform;
        this.avZones = avZones;
    }

    public List<String> avZones() {
        return avZones;
    }

    public static Set<String> filterRegionsByPlatform(String cloudPlatform) {
        Set<String> regions = new HashSet<>();
        for (Region region : Region.values()) {
            if (region.cloudPlatform.equals(cloudPlatform)) {
                regions.add(region.name());
            }
        }
        return regions;
    }

    public static Set<String> filterAvZones(String cloudPlatform) {
        Set<String> avZonesSet = new HashSet<>();
        for (Region region : Region.values()) {
            if (region.cloudPlatform.equals(cloudPlatform)) {
                avZonesSet.addAll(region.avZones);
            }
        }
        return avZonesSet;
    }

    public static boolean regionContainsAvZone(String region, String avZone) {
        for (Region region1 : Region.values()) {
            if (region1.name().equals(region)) {
                if (region1.avZones.contains(avZone)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }
}
