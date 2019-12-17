package com.incountry;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MigrateResult {
    private int migrated;
    private int totalLeft;
}
