package com.incountry.residence.sdk.tools.transfer;

public class TransferPop {
    String name;
    String id;
    String status;
    boolean direct;

    @Override
    public String toString() {
        return "TransferPop{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", direct=" + direct +
                '}';
    }

    public String getId() {
        return id.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public boolean isDirect() {
        return direct;
    }
}
