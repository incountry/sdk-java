package com.incountry;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MigrateResult {
    private int migrated;
    private int totalLeft;
}
