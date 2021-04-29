package com.incountry.residence.sdk.tools.dao;

/**
 * class-container to store host list with PoPAPI
 */
public class POP {
    private final String host;
    private final String name;
    private final String region;
    private final boolean isMidPop;

    public POP(String host, String name, String region, boolean isMidPop) {
        this.host = host;
        this.name = name;
        this.region = region;
        this.isMidPop = isMidPop;
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

    public boolean isMidPop() {
        return isMidPop;
    }

    @Override
    public String toString() {
        return "POP{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", isMidPop='" + isMidPop + '\'' +
                '}';
    }
}
