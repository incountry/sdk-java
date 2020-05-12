package com.incountry.residence.sdk.tools.dao;

/**
 * class-container to store host list with PoPAPI
 */
public class PoP {
    private String host;
    private String name;

    public PoP(String host, String name) {
        this.host = host;
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PoP{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
