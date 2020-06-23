package com.incountry.residence.sdk.tools.dao;

/**
 * class-container to store host list with PoPAPI
 */
public class POP {
    private final String host;
    private final String name;
    private final String region;

    public POP(String host, String name, String region) {
        this.host = host;
        this.name = name;
        this.region = region;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getRegion(String defaultRegion) {
        return region != null ? region : defaultRegion;
    }

    @Override
    public String toString() {
        return "POP{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
