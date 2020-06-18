package com.incountry.residence.sdk.dto;

public class MigrateResult {
    private int migrated;
    private int totalLeft;

    public MigrateResult(int migrated, int totalLeft) {
        this.migrated = migrated;
        this.totalLeft = totalLeft;
    }

    public int getMigrated() {
        return migrated;
    }

    public int getTotalLeft() {
        return totalLeft;
    }

    @Override
    public String toString() {
        return "MigrateResult{" +
                "migrated=" + migrated +
                ", totalLeft=" + totalLeft +
                '}';
    }
}
