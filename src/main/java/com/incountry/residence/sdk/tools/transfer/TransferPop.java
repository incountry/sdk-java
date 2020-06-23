package com.incountry.residence.sdk.tools.transfer;

public class TransferPop {
    String name;
    String id;
    String status;
    String region;
    boolean direct;

    @Override
    public String toString() {
        return "TransferPop{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", region='" + region + '\'' +
                ", direct=" + direct +
                '}';
    }

    public String getId() {
        return id != null ? id.toLowerCase() : null;
    }

    public String getName() {
        return name;
    }

    public boolean isDirect() {
        return direct;
    }

    public String getRegion() {
        return region != null ? region.toLowerCase() : null;
    }
}
