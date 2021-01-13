package com.incountry.residence.sdk.dto;

import com.incountry.residence.sdk.tools.exceptions.RecordException;

import java.util.List;

public class MigrateResult {
    private final int migrated;
    private final int totalLeft;
    private final List<RecordException> errors;

    public MigrateResult(int migrated, int totalLeft, List<RecordException> errors) {
        this.migrated = migrated;
        this.totalLeft = totalLeft;
        this.errors = errors;
    }

    public int getMigrated() {
        return migrated;
    }

    public int getTotalLeft() {
        return totalLeft;
    }

    public List<RecordException> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "MigrateResult{" +
                "migrated=" + migrated +
                ", totalLeft=" + totalLeft +
                ", errors=" + errors +
                '}';
    }
}
