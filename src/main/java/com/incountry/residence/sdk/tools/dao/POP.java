package com.incountry.residence.sdk.tools.dao;

/**
 * class-container to store host list with POP API
 */
public class POP {
    private String host;
    private String name;

    public POP(String host, String name) {
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
        return "POP{" +
                "host='" + host + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
